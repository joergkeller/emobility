package edu.jke.emobility.adapter.util;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThat;

public class FormEncoderTest {

    @Test
    void singleField_urlEncoded() {
        FormEncoder encoder = new FormEncoder().addField("name", "dummy");
        assertThat(encoder.asUrlEncoded()).isEqualTo("name=dummy");
    }

    @Test
    void multipleFields_urlEncoded() {
        FormEncoder encoder = new FormEncoder()
                .addField("name", "dummy")
                .addField("password", "secret");
        assertThat(encoder.asUrlEncoded()).isEqualTo("name=dummy&password=secret");
    }

    @Test
    void multipleFields_sameKey_overwriteDuplicates() {
        FormEncoder encoder = new FormEncoder()
                .addField("name", "dummy")
                .addField("name", "genius");
        assertThat(encoder.asUrlEncoded()).isEqualTo("name=genius");
    }

    @Test
    void specialCharacter_urlEncoded() {
        FormEncoder encoder = new FormEncoder()
                .addField("name", "one dummy")
                .addField("mail", "one@dummy.com")
                .addField("title", "Dr&Prof=genius");
        assertThat(encoder.asUrlEncoded()).isEqualTo("name=one+dummy&mail=one%40dummy.com&title=Dr%26Prof%3Dgenius");
    }

    @Test
    void urlEncodedBytes() {
        FormEncoder encoder = new FormEncoder()
                .addField("name", "dummy")
                .addField("password", "secret");
        assertThat(encoder.asUrlEncodedBytes()).isEqualTo("name=dummy&password=secret".getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void numberField() {
        FormEncoder encoder = new FormEncoder().addField("answer", 42);
        assertThat(encoder.asUrlEncoded()).isEqualTo("answer=42");
    }

    @Test
    void dateField() {
        LocalDateTime timestamp = LocalDateTime.of(2022, Month.APRIL, 1, 22, 23, 24);
        FormEncoder encoder = new FormEncoder().addField("date", timestamp);
        assertThat(encoder.asUrlEncoded()).isEqualTo("date=2022-04-01T22%3A23%3A24");
    }

}
