package edu.jke.emobility.domain.value;

import java.util.UUID;

public class LoadSessionId {
    private final UUID value;

    public LoadSessionId(String literal) {
        this.value = UUID.fromString(literal);
    }

    public LoadSessionId() {
        this.value = UUID.randomUUID();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
