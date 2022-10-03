package edu.jke.emobility.adapter.util;

import com.fasterxml.jackson.core.JsonParser;
import edu.jke.emobility.adapter.util.DurationDeserializer;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

public class DurationDeserializerTest {

    @Test
    void deserializeSpan() throws IOException {
        JsonParser parser = Mockito.mock(JsonParser.class);
        Mockito.when(parser.getText()).thenReturn("1:15:03");
        Duration duration = new DurationDeserializer().deserialize(parser, null);
        assertThat(duration).isEqualTo(Duration.parse("PT1H15M03S"));
    }

    @Test
    void deserializeSpan_singleDigits() throws IOException {
        JsonParser parser = Mockito.mock(JsonParser.class);
        Mockito.when(parser.getText()).thenReturn("1:5:3");
        Duration duration = new DurationDeserializer().deserialize(parser, null);
        assertThat(duration).isEqualTo(Duration.parse("PT1H5M3S"));
    }

    @Test
    void deserializeSpan_missingHours() throws IOException {
        JsonParser parser = Mockito.mock(JsonParser.class);
        Mockito.when(parser.getText()).thenReturn("1:15");
        Duration duration = new DurationDeserializer().deserialize(parser, null);
        assertThat(duration).isEqualTo(Duration.parse("PT1M15S"));
    }

    @Test
    void deserializeSpan_missingHoursAndMinutes() throws IOException {
        JsonParser parser = Mockito.mock(JsonParser.class);
        Mockito.when(parser.getText()).thenReturn("15");
        Duration duration = new DurationDeserializer().deserialize(parser, null);
        assertThat(duration).isEqualTo(Duration.parse("PT15S"));
    }

    @Test
    void deserializeSpan_moreThan24hours() throws IOException {
        JsonParser parser = Mockito.mock(JsonParser.class);
        Mockito.when(parser.getText()).thenReturn("35:25:33");
        Duration duration = new DurationDeserializer().deserialize(parser, null);
        assertThat(duration).isEqualTo(Duration.parse("PT35H25M33S"));
    }

    @Test
    void deserializeSpan_dayFormat() throws IOException {
        JsonParser parser = Mockito.mock(JsonParser.class);
        Mockito.when(parser.getText()).thenReturn("1.11:25:33");
        Duration duration = new DurationDeserializer().deserialize(parser, null);
        assertThat(duration).isEqualTo(Duration.parse("PT35H25M33S"));
    }
}
