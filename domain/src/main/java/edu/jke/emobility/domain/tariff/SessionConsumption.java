package edu.jke.emobility.domain.tariff;

import edu.jke.emobility.domain.session.LoadSession;

import javax.measure.Quantity;
import javax.measure.quantity.Energy;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

public record SessionConsumption(
        LoadSession session,
        Quantity<Energy> basicEnergy,
        Quantity<Energy> elevatedEnergy,
        List<EnergyConsumption> detailConsumptions
) {

    public static Comparator<SessionConsumption> BY_START = Comparator.comparing(sc -> sc.session().chargingStart());

    /**
     * Aggregate the list of consumptions into fixed time intervals starting before the session start.
     */
    public List<EnergyConsumption> convertInterval(int minutes) {
        LocalDateTime start = session().chargingStart()
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
        return EnergyConsumption.convertInterval(detailConsumptions(), start, minutes);
    }

}
