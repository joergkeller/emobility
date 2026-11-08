package edu.jke.emobility.domain.session;

import edu.jke.emobility.domain.value.LoadSessionId;
import edu.jke.emobility.domain.value.UserIdentification;

import javax.measure.Quantity;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Power;
import java.time.ZonedDateTime;

import static edu.jke.emobility.domain.value.CustomUnits.WATT_HOUR;
import static javax.measure.MetricPrefix.KILO;

public record LoadSession(
        LoadSessionId sessionId,
        ZonedDateTime chargingStart,
        ZonedDateTime chargingEnd,
        UserIdentification userIdentification,
        Quantity<Energy> energy,
        Quantity<Power> maxPower,
        String mode,
        String stopReason
) {

    public LoadSession(ZonedDateTime chargingStart, ZonedDateTime chargingEnd, UserIdentification userIdentification, Quantity<Energy> energy, Quantity<Power> maxPower, String mode, String stopReason) {
        this(new LoadSessionId(), chargingStart, chargingEnd, userIdentification, energy, maxPower, mode, stopReason);
    }

    public boolean isValid() {
//        if (userIdentification.name().equals("Unknown")) return false;
        return energy.to(KILO(WATT_HOUR)).getValue().longValue() <= 1000;
    }
}
