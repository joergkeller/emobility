package edu.jke.emobility.domain.session;

import edu.jke.emobility.domain.value.ChargerId;
import edu.jke.emobility.domain.value.Energy;
import edu.jke.emobility.domain.value.LoadSessionId;

import java.time.LocalDateTime;

import static java.util.Objects.requireNonNull;

public class LoadSession {
    private final LoadSessionId id = new LoadSessionId();
    private final LocalDateTime chargingStart;
    private final LocalDateTime chargingEnd;
    private final ChargerId chargerId;
    private final Energy energy;

    public LoadSession(LocalDateTime chargingStart, LocalDateTime chargingEnd, ChargerId chargerId, Energy energy) {
        this.chargingStart = requireNonNull(chargingStart);
        this.chargingEnd = chargingEnd;
        this.chargerId = requireNonNull(chargerId);
        this.energy = requireNonNull(energy);
    }

    public LoadSessionId getId() {
        return id;
    }

    public LocalDateTime getChargingStart() {
        return chargingStart;
    }

    public LocalDateTime getChargingEnd() {
        return chargingEnd;
    }

    public ChargerId getChargerId() {
        return chargerId;
    }

    public Energy getEnergy() {
        return energy;
    }
}
