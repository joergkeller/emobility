package edu.jke.emobility.domain.value;

import java.util.Objects;

public class ChargerId {
    private final String value;

    public ChargerId(String literal) {
        this.value = literal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChargerId chargerId = (ChargerId) o;
        return value.equals(chargerId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
