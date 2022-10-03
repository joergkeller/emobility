package edu.jke.emobility.adapter.station;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import edu.jke.emobility.adapter.error.AuthenticationException;
import edu.jke.emobility.adapter.error.ServiceException;
import edu.jke.emobility.adapter.util.FormEncoder;
import edu.jke.emobility.domain.session.LoadSession;
import edu.jke.emobility.domain.value.ChargerId;
import edu.jke.emobility.domain.value.Energy;
import edu.jke.emobility.usecase.session.RequestLoadSessionsUC;
import edu.jke.emobility.usecase.session.StationEndpoint;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class StationAdapter implements StationEndpoint {

    private static Logger log = LoggerFactory.getLogger(StationAdapter.class);

    private static ObjectMapper objectMapper = new ObjectMapper();

    private final OkHttpClient client = new OkHttpClient().newBuilder().protocols(Arrays.asList(Protocol.HTTP_1_1)).build();
    private String username;
    private String password;
    private String token;

    {
        objectMapper.registerModule(new JavaTimeModule());
    }

    public StationAdapter login(String hostname, String username, String password) {
        RequestBody body = RequestBody.create(new FormEncoder()
                        .addField("email", username)
                        .addField("password", password)
                        .asUrlEncodedBytes());
        Request request = new Request.Builder()
                .url("http://" + hostname + "/api/webOperatorLogin/")
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

    private void consumeLoginResponse(String json) throws JsonProcessingException {
        LoginResponseEntity loginResponse = objectMapper.readValue(json, LoginResponseEntity.class);
        if (loginResponse.getErrorCode() != 0) throw new AuthenticationException(loginResponse.getErrorMessage());
        token = loginResponse.getToken();
    }

    public String loadSessions(String hostname, LocalDateTime from, LocalDateTime to) {
        if (token == null || token.isBlank()) throw new AuthenticationException("Missing token");
        RequestBody body = RequestBody.create(new FormEncoder()
                        .addField("pageSize", 100)
                        .addField("pageNumber", 1)
                        .addField("chargingStartedTimeFrom", from)
                        .addField("chargingStartedTimeTo", to)
                        .addField("orderByColumn", "chargingStartedTime")
                        .addField("orderDirection", "Descending")
                        .asUrlEncodedBytes());
        Request request = new Request.Builder()
                .url("http://" + hostname + "/api/chargingSession/")
                .method("POST", body)
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();
        log.debug(request.toString().replaceAll("Authorization:.*?,", "Authorization:***,"));
        try (Response response = client.newCall(request).execute()) {
            log.info("Load sessions: {} ({})", response.message(), response.code());
            if (!response.isSuccessful()) throw new ServiceException(response.message());
            String json = response.body().string();
            log.info("Found: {}", json.replaceAll("\\s+", " "));
            return json;
        } catch (IOException ex) {
            throw new ServiceException(ex);
        }
    }

    public String getToken() {
        return token;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public List<LoadSession> importSessions(String stationName, LocalDateTime from, LocalDateTime to) {
        login(stationName, this.username, this.password);
        String json = loadSessions(stationName, from, to);
        return parseSessionResponse(json).stream()
                .map(this::asLoadSession)
                .collect(Collectors.toList());
    }

    @NotNull
    List<SessionEntity> parseSessionResponse(String json) {
        try {
            SessionResponseEntity sessionResponse = objectMapper.readValue(json, SessionResponseEntity.class);
            if (sessionResponse.getErrorCode() != 0) {
                throw new ServiceException(sessionResponse.getErrorMessage());
            }
            return sessionResponse.getContent();
        } catch (IOException e) {
            throw new ServiceException(e);
        }
    }

    LoadSession asLoadSession(SessionEntity entity) {
        return new LoadSession(
                entity.getChargingStartedTime().withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime(),
                entity.getChargingEndedTime().withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime(),
                new ChargerId(String.format("%s %s", entity.getUser().getFirstName(), entity.getUser().getLastName())),
                Energy.kWh(entity.getActiveEnergyConsumed()));
    }

}
