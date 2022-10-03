package edu.jke.emobility.adapter.station;

import edu.jke.emobility.domain.session.LoadSession;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class StationAdapterTest {

    private static String json;
    private static MockWebServer server = new MockWebServer();

    private StationAdapter stationAdapter = new StationAdapter();

    @BeforeAll
    static void beforeAll() throws IOException {
        json = Files.readString(Path.of("src/test/resources/sessionResponse.json"));
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
        server.enqueue(new MockResponse().setBody(json));
        String resultString = stationAdapter
                .login(hostnameOf(url), "xxx.yyy", "zzz")
                .loadSessions(hostnameOf(url), LocalDateTime.now().minusDays(30), LocalDateTime.now());
        assertThat(resultString).isEqualTo(json);
        assertThat(resultString).contains("LastName\": \"16\"");
    }

    @Test
    void parseSessionResponse() {
        List<SessionEntity> sessions = stationAdapter.parseSessionResponse(json);
        assertThat(sessions.size()).isEqualTo(8);
        assertThat(sessions.get(0).getChargingSessionId()).isEqualTo(10);
        assertThat(sessions.get(0).getUser().getFirstName()).isEqualTo("Parkplatz");
        assertThat(sessions.get(0).getUser().getLastName()).isEqualTo("16");
    }

    @Test
    void asLoadSession() {
        UserEntity user = new UserEntity("Last", "First");
        ZonedDateTime start = ZonedDateTime.now();
        SessionEntity entity = new SessionEntity(user, 123, start, null, null, 0, 0, 12.5);
        LoadSession session = stationAdapter.asLoadSession(entity);
        assertThat(session.getChargerId().toString()).isEqualTo("<First Last>");
        assertThat(session.getChargingStart()).isEqualTo(start.toLocalDateTime());
        assertThat(session.getEnergy().askWh()).isEqualTo(12.5);
    }
}
