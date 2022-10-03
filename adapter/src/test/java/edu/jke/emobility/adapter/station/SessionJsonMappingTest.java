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
        assertThat(session.getChargingSessionId()).isEqualTo(10);
        assertThat(session.getUser().getFirstName()).isEqualTo("Parkplatz");
        assertThat(session.getUser().getLastName()).isEqualTo("16");
    }

    @Test
    void mapMultipleSessionsJson() throws IOException {
        String json = Files.readString(Path.of("src/test/resources/multipleSessions.json"));
        SessionEntity[] sessions = mapper.readValue(json, SessionEntity[].class);
        assertThat(sessions.length).isEqualTo(8);
        assertThat(sessions[0].getChargingSessionId()).isEqualTo(10);
        assertThat(sessions[1].getChargingSessionId()).isEqualTo(9);
    }

    @Test
    void mapSessionResponseJson() throws IOException {
        String json = Files.readString(Path.of("src/test/resources/sessionResponse.json"));
        SessionResponseEntity response = mapper.readValue(json, SessionResponseEntity.class);
        assertThat(response.getErrorCode()).isEqualTo(0);
        assertThat(response.getContent().size()).isEqualTo(8);
        assertThat(response.getPagingInfo().getNumberOfRows()).isEqualTo(response.getContent().size());
        assertThat(response.getPagingInfo().getPageCount()).isEqualTo(1);
    }

}
