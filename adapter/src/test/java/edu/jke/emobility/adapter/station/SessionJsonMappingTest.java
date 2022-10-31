package edu.jke.emobility.adapter.station;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class SessionJsonMappingTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void initialize() {
        mapper.registerModule(new JavaTimeModule());
    }


    @Test
    void mapSingleSessionJson() throws IOException {
        String json = Files.readString(Path.of("src/test/resources/singleSession.json"));
        SessionEntity session = mapper.readValue(json, SessionEntity.class);
        assertThat(session.chargingSessionId()).isEqualTo(10);
        assertThat(session.user().firstName()).isEqualTo("Parkplatz");
        assertThat(session.user().lastName()).isEqualTo("16");
    }

    @Test
    void mapMultipleSessionsJson() throws IOException {
        String json = Files.readString(Path.of("src/test/resources/multipleSessions.json"));
        SessionEntity[] sessions = mapper.readValue(json, SessionEntity[].class);
        assertThat(sessions.length).isEqualTo(8);
        assertThat(sessions[0].chargingSessionId()).isEqualTo(10);
        assertThat(sessions[1].chargingSessionId()).isEqualTo(9);
    }

    @Test
    void mapSessionResponseJson() throws IOException {
        String json = Files.readString(Path.of("src/test/resources/sessionResponse.json"));
        StationAdapter.SessionResponseEntity response = mapper.readValue(json, StationAdapter.SessionResponseEntity.class);
        assertThat(response.errorCode()).isEqualTo(0);
        assertThat(response.content().size()).isEqualTo(8);
        assertThat(response.pagingInfo().numberOfRows()).isEqualTo(response.content().size());
        assertThat(response.pagingInfo().pageCount()).isEqualTo(1);
    }

}
