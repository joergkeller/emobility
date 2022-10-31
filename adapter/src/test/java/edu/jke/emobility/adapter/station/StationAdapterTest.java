package edu.jke.emobility.adapter.station;

import edu.jke.emobility.domain.session.LoadSession;
import edu.jke.emobility.domain.session.PowerMeasure;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tech.units.indriya.unit.Units;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class StationAdapterTest {

    private static String sessionJson;
    private static String profileJson;
    private static final MockWebServer server = new MockWebServer();

    private final StationAdapter stationAdapter = new StationAdapter();

    @BeforeAll
    static void beforeAll() throws IOException {
        sessionJson = Files.readString(Path.of("src/test/resources/sessionResponse.json"));
        profileJson = Files.readString(Path.of("src/test/resources/profileResponse.json"));
        server.start();
    }

    @AfterAll
    static void afterAll() throws IOException {
        server.shutdown();
    }

    private String hostnameOf(HttpUrl url) {
        return url.host() + ":" + url.port();
    }

    @Test
    void login() {
        HttpUrl url = server.url("/api/webOperatorLogin/");
        server.enqueue(new MockResponse().setBody("{\"token\": \"thisisatoken\"}"));
        stationAdapter.login(hostnameOf(url), "xxx.yyy", "zzz");
        assertThat(stationAdapter.getToken()).isEqualTo("thisisatoken");
    }

    @Test
    void loadSessionsFromStation() {
        HttpUrl url = server.url("/api/chargingSession/");
        server.enqueue(new MockResponse().setBody("{\"token\": \"thisisatoken\"}"));
        server.enqueue(new MockResponse().setBody(sessionJson));
        String resultString = stationAdapter
                .login(hostnameOf(url), "xxx.yyy", "zzz")
                .loadSessions(hostnameOf(url), ZonedDateTime.now().minusDays(30), ZonedDateTime.now());
        assertThat(resultString).isEqualTo(sessionJson);
        assertThat(resultString).contains("LastName\": \"16\"");
    }

    @Test
    void loadProfileFromStation() {
        HttpUrl url = server.url("/api/powerProfile/");
        server.enqueue(new MockResponse().setBody("{\"token\": \"thisisatoken\"}"));
        server.enqueue(new MockResponse().setBody(profileJson));
        String resultString = stationAdapter
                .login(hostnameOf(url), "xxx.yyy", "zzz")
                .loadProfile(hostnameOf(url), ZonedDateTime.now().minusHours(24), ZonedDateTime.now());
        assertThat(resultString).isEqualTo(profileJson);
        assertThat(resultString).contains("\"connector1\":");
    }

    @Test
    void parseSessionResponse() {
        List<SessionEntity> sessions = stationAdapter.parseSessionResponse(sessionJson);
        assertThat(sessions.size()).isEqualTo(8);
        assertThat(sessions.get(0).chargingSessionId()).isEqualTo(10);
        assertThat(sessions.get(0).user().firstName()).isEqualTo("Parkplatz");
        assertThat(sessions.get(0).user().lastName()).isEqualTo("16");
        assertThat(sessions.get(0).activeEnergyConsumed()).isEqualTo(21.467152);
        assertThat(sessions.get(0).maxSessionPower()).isEqualTo(3.392);
    }

    @Test
    void parseProfileResponse() {
        List<ProfileEntity> profiles = stationAdapter.parseProfileResponse(profileJson);
        assertThat(profiles.size()).isEqualTo(72);
        assertThat(profiles.get(0).time()).isEqualTo(ZonedDateTime.parse("2022-10-06T21:10:00+00:00"));
        assertThat(profiles.get(0).connectorPower()).isEqualTo(10.304);
    }

    @Test
    void asPowerProfile() {
        ProfileEntity entity = new ProfileEntity(ZonedDateTime.parse("2022-10-06T21:10:00+00:00"), 10.3);
        PowerMeasure measure = stationAdapter.asPowerMeasure(entity);
        assertThat(measure.time().getHour()).isEqualTo(23);
        assertThat(measure.power().to(Units.WATT).getValue().intValue()).isEqualTo(10300);
    }

    @Test
    void asLoadSession() {
        UserIdentification identification = new UserIdentification("RfidCard", "1234567890", "0055");
        UserEntity user = new UserEntity("Last", "First");
        ZonedDateTime start = ZonedDateTime.now();
        SessionEntity entity = new SessionEntity(identification, user, 123, start, null, null, 0, 0, 12.5, 3.7, "Fast", "LocalStopUserDisconnectedCableFromVehicle");
        LoadSession session = stationAdapter.asLoadSession(entity);
        assertThat(session.userIdentification().name()).isEqualTo("First Last");
        assertThat(session.chargingStart()).isEqualTo(start.toLocalDateTime());
        assertThat(session.energy().toString()).isEqualTo("12.5 kWh");
        assertThat(session.maxPower().toString()).isEqualTo("3.7 kW");
        assertThat(session.mode()).isEqualTo("Fast");
        assertThat(session.stopReason()).isEqualTo("LocalStopUserDisconnectedCableFromVehicle");
    }
}
