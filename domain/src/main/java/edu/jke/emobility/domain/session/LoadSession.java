package edu.jke.emobility.domain.session;

import edu.jke.emobility.domain.value.ChargerId;
import edu.jke.emobility.domain.value.LoadSessionId;

import javax.measure.Quantity;
import javax.measure.quantity.Energy;
import java.time.LocalDateTime;

import static java.util.Objects.requireNonNull;

public class LoadSession {
    private final LoadSessionId id = new LoadSessionId();
    private final LocalDateTime chargingStart;
    private final LocalDateTime chargingEnd;
    private final ChargerId chargerId;
    private final Quantity<Energy> energy;

    public LoadSession(LocalDateTime chargingStart, LocalDateTime chargingEnd, ChargerId chargerId, Quantity<Energy> energy) {
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

    public Quantity<Energy> getEnergy() {
        return energy;
    }

}
