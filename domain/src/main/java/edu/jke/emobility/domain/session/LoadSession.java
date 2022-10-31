package edu.jke.emobility.domain.session;

import edu.jke.emobility.domain.value.LoadSessionId;
import edu.jke.emobility.domain.value.UserIdentification;

import javax.measure.Quantity;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Power;
import java.time.LocalDateTime;

public record LoadSession(
        LoadSessionId sessionId,
        LocalDateTime chargingStart,
        LocalDateTime chargingEnd,
        UserIdentification userIdentification,
        Quantity<Energy> energy,
        Quantity<Power> maxPower,
        String mode,
        String stopReason
) {

    public LoadSession(LocalDateTime chargingStart, LocalDateTime chargingEnd, UserIdentification userIdentification, Quantity<Energy> energy, Quantity<Power> maxPower, String mode, String stopReason) {
        this(new LoadSessionId(), chargingStart, chargingEnd, userIdentification, energy, maxPower, mode, stopReason);
    }

}
