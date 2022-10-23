package edu.jke.emobility.domain.session;

import edu.jke.emobility.domain.error.ConsistencyException;
import edu.jke.emobility.domain.tariff.SessionConsumption;
import edu.jke.emobility.domain.value.ChargerId;
import edu.jke.emobility.domain.value.CustomUnits;

import javax.measure.Quantity;
import javax.measure.quantity.Energy;
import java.time.LocalDateTime;

import static edu.jke.emobility.domain.util.DateUtil.earliest;
import static edu.jke.emobility.domain.util.DateUtil.latest;

/**
 * Summarize charging session energy consumption over a given period.
 */
public record SessionSummary(
        ChargerId chargerId,
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
        if (chargerId == null || chargerId.equals(consumption.session().getChargerId())) {
            return new SessionSummary(
                    consumption.session().getChargerId(),
                    earliest(start, consumption.session().getChargingStart()),
                    latest(end, consumption.session().getChargingEnd()),
                    sessionCount + 1,
                    energy.add(consumption.session().getEnergy()),
                    basicEnergy.add(consumption.basicEnergy()),
                    elevatedEnergy.add(consumption.elevatedEnergy()));
        } else {
            throw new ConsistencyException("Mismatching charger ids %s and %s".formatted(chargerId, consumption.session().getChargerId()));
        }
    }

    public boolean isValid() {
        return chargerId != null;
    }
}
