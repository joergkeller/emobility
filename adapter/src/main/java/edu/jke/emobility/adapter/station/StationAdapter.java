package edu.jke.emobility.adapter.station;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import edu.jke.emobility.adapter.util.FormEncoder;
import edu.jke.emobility.domain.session.LoadSession;
import edu.jke.emobility.domain.session.PowerMeasure;
import edu.jke.emobility.domain.session.PowerProfile;
import edu.jke.emobility.domain.util.EnergyUtil;
import edu.jke.emobility.domain.value.UserIdentification;
import edu.jke.emobility.usecase.error.AuthenticationException;
import edu.jke.emobility.usecase.error.ServiceException;
import edu.jke.emobility.usecase.session.StationEndpoint;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.units.indriya.quantity.Quantities;

import javax.measure.Quantity;
import javax.measure.quantity.Power;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static javax.measure.MetricPrefix.KILO;
import static tech.units.indriya.unit.Units.WATT;

public class StationAdapter implements StationEndpoint {

    private static final Logger log = LoggerFactory.getLogger(StationAdapter.class);

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final OkHttpClient client = new OkHttpClient().newBuilder().protocols(List.of(Protocol.HTTP_1_1)).build();
    private String username;
    private String password;
    private String token;

    static {
        objectMapper.registerModule(new JavaTimeModule());
    }

    public StationAdapter login(String stationName, String username, String password) {
        RequestBody body = RequestBody.create(new FormEncoder()
                        .addField("email", username)
                        .addField("password", password)
                        .asUrlEncodedBytes());
        Request request = new Request.Builder()
                .url("http://" + stationName + "/api/webOperatorLogin/")
                .method("POST", body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();
        log.debug(request.toString());
        try (Response response = client.newCall(request).execute()) {
            log.info("Login: {} ({})", response.message(), response.code());
            if (!response.isSuccessful()) throw new AuthenticationException(response.message());
            consumeLoginResponse(response.body().string());
        } catch (IOException ex) {
            throw new ServiceException(ex);
        }
        return this;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static record LoginResponseEntity(
        int errorCode,
        String errorMessage,
        String token
    ) {}

    private void consumeLoginResponse(String json) throws JsonProcessingException {
        Objects.requireNonNull(json);
        LoginResponseEntity loginResponse = objectMapper.readValue(json, LoginResponseEntity.class);
        if (loginResponse.errorCode() != 0) throw new AuthenticationException(loginResponse.errorMessage());
        token = loginResponse.token();
    }

    public String loadSessions(String stationName, ZonedDateTime from, ZonedDateTime to) {
        if (isLoggedOut()) throw new AuthenticationException("Missing token");
        RequestBody body = RequestBody.create(new FormEncoder()
                        .addField("pageSize", 100)
                        .addField("pageNumber", 1)
                        .addField("chargingStartedTimeFrom", from)
                        .addField("chargingStartedTimeTo", to)
                        .addField("orderByColumn", "chargingStartedTime")
                        .addField("orderDirection", "Descending")
                        .asUrlEncodedBytes());
        Request request = new Request.Builder()
                .url("http://" + stationName + "/api/chargingSession/")
                .method("POST", body)
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();
        log.debug(request.toString().replaceAll("Authorization:.*?,", "Authorization:***,"));
        try (Response response = client.newCall(request).execute()) {
            log.info("Load sessions: {} ({})", response.message(), response.code());
            if (!response.isSuccessful()) throw new ServiceException(response.message());
            String json = response.body().string();
            log.info("Sessions: {}", json.replaceAll("\\s+", " "));
            return json;
        } catch (IOException ex) {
            throw new ServiceException(ex);
        }
    }

    public String loadProfile(String stationName, ZonedDateTime from, ZonedDateTime to) {
        if (isLoggedOut()) throw new AuthenticationException("Missing token");
        String params = new FormEncoder()
                .addField("measure", "connector1")
                .addField("fromTime", from.truncatedTo(ChronoUnit.MINUTES))
                .addField("toTime", to.truncatedTo(ChronoUnit.MINUTES).plusMinutes(5)).asUrlEncoded();
        Request request = new Request.Builder()
                .url("http://" + stationName + "/api/powerProfile/?" + params)
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();
        log.debug(request.toString().replaceAll("Authorization:.*?,", "Authorization:***,"));
        try (Response response = client.newCall(request).execute()) {
            log.info("Load profile: {} ({})", response.message(), response.code());
            if (!response.isSuccessful()) throw new ServiceException(response.message());
            String json = response.body().string();
            log.info("Profile: {}", json.replaceAll("\\s+", " "));
            return json;
        } catch (IOException ex) {
            throw new ServiceException(ex);
        }
    }

    private boolean isLoggedOut() {
        return token == null || token.isBlank();
    }

    String getToken() {
        return token;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public void logout() {
        token = null;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static record SessionResponseEntity(
        int errorCode,
        String errorMessage,
        List<SessionEntity> content,
        PagingInfoEntity pagingInfo
    ) {}

    @NotNull
    List<SessionEntity> parseSessionResponse(String json) {
        try {
            SessionResponseEntity sessionResponse = objectMapper.readValue(json, SessionResponseEntity.class);
            if (sessionResponse.errorCode() != 0) {
                throw new ServiceException(sessionResponse.errorMessage());
            }
            return sessionResponse.content();
        } catch (IOException e) {
            throw new ServiceException(e);
        }
    }

    LoadSession asLoadSession(SessionEntity entity) {
        ZonedDateTime started = entity.chargingStartedTime();
        ZonedDateTime ended = entity.chargingEndedTime();
        UserIdentification identification = new UserIdentification();
        if (entity.user() == null) {
            log.warn("Missing user at {} loading {} kWh", started, entity.activeEnergyConsumed());
        } else {
            identification = new UserIdentification(
                    String.format("%s %s", entity.user().firstName(), entity.user().lastName()),
                    entity.userIdentification().userIdentificationType(),
                    entity.userIdentification().identificationCode(),
                    entity.userIdentification().number()
            );
        }
        return new LoadSession(
                started.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime(),
                ended == null ? null : ended.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime(),
                identification,
                EnergyUtil.kWh(entity.activeEnergyConsumed()),
                EnergyUtil.kW(entity.maxSessionPower()),
                entity.chargingMode() == null ? "Normal" : entity.chargingMode(),
                entity.chargingSessionStopReason()
        );
    }

    @Override
    public List<LoadSession> importSessions(String stationName, LocalDateTime from, LocalDateTime to) {
        if (isLoggedOut()) login(stationName, this.username, this.password);

        ZoneId zoneId = ZoneId.systemDefault();
        ZonedDateTime start = ZonedDateTime.of(from, zoneId);
        ZonedDateTime end = ZonedDateTime.of(to, zoneId);
        String json = loadSessions(stationName, start, end);
        return parseSessionResponse(json).stream()
                .map(this::asLoadSession)
                .collect(Collectors.toList());
    }

    List<ProfileEntity> parseProfileResponse(String json) {
        try {
            ProfileEntity[] profiles = objectMapper.readValue(json, ProfileEntity[].class);
            return List.of(profiles);
        } catch (IOException e) {
            throw new ServiceException(e);
        }
    }

    PowerMeasure asPowerMeasure(ProfileEntity entity) {
        LocalDateTime time = entity.time().withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
        Quantity<Power> power = Quantities.getQuantity(entity.connectorPower(), KILO(WATT));
        return new PowerMeasure(time, power);
    }

    @Override
    public PowerProfile importProfile(String stationName, LoadSession session) {
        if (isLoggedOut()) login(stationName, this.username, this.password);

        LocalDateTime chargingStart = session.chargingStart();
        LocalDateTime chargingEnd = session.chargingEnd();
        ZoneId zoneId = ZoneId.systemDefault();
        ZonedDateTime start = ZonedDateTime.of(chargingStart, zoneId);
        ZonedDateTime end = chargingEnd != null ? ZonedDateTime.of(chargingEnd, zoneId) : ZonedDateTime.now();
        String json = loadProfile(stationName, start, end);
        List<PowerMeasure> measures = parseProfileResponse(json).stream()
                .map(this::asPowerMeasure)
                .collect(Collectors.toList());
        return new PowerProfile(session, measures);
    }

}
