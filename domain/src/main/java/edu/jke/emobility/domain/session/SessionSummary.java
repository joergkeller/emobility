package edu.jke.emobility.domain.session;

import edu.jke.emobility.domain.error.ConsistencyException;
import edu.jke.emobility.domain.value.ChargerId;
import edu.jke.emobility.domain.value.Energy;

import java.time.LocalDateTime;

public record SessionSummary(ChargerId chargerId, LocalDateTime start, LocalDateTime end, Energy energy) {

    public SessionSummary add(LoadSession session) {
        if (chargerId == null || chargerId.equals(session.getChargerId())) {
            return new SessionSummary(
                    session.getChargerId(),
                    earliest(start, session.getChargingStart()),
                    latest(end, session.getChargingEnd()),
                    energy.add(session.getEnergy()));
        } else {
            throw new ConsistencyException("Mismatching charger ids %s and %s".formatted(chargerId, session.getChargerId()));
        }
    }

    public LocalDateTime earliest(LocalDateTime left, LocalDateTime right) {
        if (left == null || left.isAfter(right)) {
            return right;
        } else {
            return left;
        }
    }

    public LocalDateTime latest(LocalDateTime left, LocalDateTime right) {
        if (left == null || left.isBefore(right)) {
            return right;
        } else {
            return left;
        }
    }

    public boolean isValid() {
        return chargerId != null;
    }
}
