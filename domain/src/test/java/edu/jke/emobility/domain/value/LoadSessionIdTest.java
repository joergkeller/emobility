package edu.jke.emobility.domain.value;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LoadSessionIdTest {

    @Test
    void random() {
        // TODO: regex check
        assertEquals(36, new LoadSessionId().toString().length());
    }

    @Test
    void nullValue() {
        assertThrows(NullPointerException.class, () -> {
            new LoadSessionId(null);
        });
    }

    @Test
    void illegalValue() {
        assertThrows(IllegalArgumentException.class, () -> {
            new LoadSessionId("dummy");
        });
    }

    @Test
    void valid() {
        String literal = "6b3d9860-cbec-4ee5-b37a-5ed3fd64cfe9";
        assertEquals(literal, new LoadSessionId(literal).toString());
    }

    @Test
    void abbreviatedValid() {
        String in = "0-0-0-0-0";
        assertEquals("00000000-0000-0000-0000-000000000000", new LoadSessionId(in).toString());
    }
}
