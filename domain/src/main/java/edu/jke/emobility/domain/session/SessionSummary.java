package edu.jke.emobility.domain.session;

import edu.jke.emobility.domain.error.ConsistencyException;
import edu.jke.emobility.domain.tariff.SessionConsumption;
import edu.jke.emobility.domain.value.CustomUnits;
import edu.jke.emobility.domain.value.UserIdentification;

import javax.measure.Quantity;
import javax.measure.quantity.Energy;
import java.time.LocalDateTime;

import static edu.jke.emobility.domain.util.DateUtil.earliest;
import static edu.jke.emobility.domain.util.DateUtil.latest;

/**
 * Summarize charging session energy consumption over a given period.
 */
public record SessionSummary(
        UserIdentification userIdentification,
        LocalDateTime start,
        LocalDateTime end,
        long sessionCount,
        Quantity<Energy> energy,
        Quantity<Energy> basicEnergy,
        Quantity<Energy> elevatedEnergy)
{
    public SessionSummary() {
        this(null, null, null, 0, CustomUnits.ZERO_ENERGY, CustomUnits.ZERO_ENERGY, CustomUnits.ZERO_ENERGY);
    }

    public SessionSummary add(SessionConsumption consumption) {
        if (userIdentification == null || userIdentification.equals(consumption.session().userIdentification())) {
            return new SessionSummary(
                    consumption.session().userIdentification(),
                    earliest(start, consumption.session().chargingStart()),
                    latest(end, consumption.session().chargingEnd()),
                    sessionCount + 1,
                    energy.add(consumption.session().energy()),
                    basicEnergy.add(consumption.basicEnergy()),
                    elevatedEnergy.add(consumption.elevatedEnergy()));
        } else {
            throw new ConsistencyException("Mismatching charger ids %s and %s".formatted(userIdentification, consumption.session().userIdentification()));
        }
    }

    public boolean isValid() {
        return userIdentification != null;
    }
}
