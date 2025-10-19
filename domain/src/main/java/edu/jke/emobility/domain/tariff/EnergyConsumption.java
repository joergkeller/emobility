package edu.jke.emobility.domain.tariff;

import edu.jke.emobility.domain.util.DateUtil;

import javax.measure.Quantity;
import javax.measure.quantity.Energy;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static edu.jke.emobility.domain.value.CustomUnits.ZERO_ENERGY;

/**
 * Energy consumption in a time interval with tariff information.
 */
public record EnergyConsumption(
        LocalDateTime startTime,
        LocalDateTime endTime,
        TariffSetting.Tariff tariff,
        Quantity<Energy> energy
) {

    public EnergyConsumption merge(EnergyConsumption other) {
        return new EnergyConsumption(
                DateUtil.earliest(this.startTime, other.startTime),
                DateUtil.latest(this.endTime, other.endTime),
                this.tariff,
                this.energy.add(other.energy));
    }

    /**
     * Scale the energy consumption by a factor to adjust for errors.
     */
    public static List<EnergyConsumption> multiply(List<EnergyConsumption> consumptions, Number scalingFactor) {
        return consumptions.stream()
                .map(c -> new EnergyConsumption(c.startTime, c.endTime, c.tariff, c.energy.multiply(scalingFactor)))
                .toList();
    }

    /**
     * Aggregate a list of consumptions into fixed time intervals.
     */
    public static List<EnergyConsumption> convertInterval(List<EnergyConsumption> consumptions, LocalDateTime start, int minutes) {
        if (consumptions.isEmpty()) return List.of();

        EnergyConsumption currentInterval = new EnergyConsumption(start, start.plusMinutes(minutes), consumptions.getFirst().tariff, ZERO_ENERGY);
        ArrayList<EnergyConsumption> aggregated = new ArrayList<>();
        for (EnergyConsumption consumption : consumptions) {
            while (consumption.startTime().isAfter(currentInterval.endTime)) {
                // trailing empty interval
                aggregated.add(currentInterval);
                LocalDateTime nextStart = currentInterval.endTime;
                currentInterval = new EnergyConsumption(nextStart, nextStart.plusMinutes(minutes), consumption.tariff, ZERO_ENERGY);
            }
            if (consumption.startTime().isBefore(currentInterval.endTime)) {
                // current interval
                currentInterval = currentInterval.merge(consumption);
            } else {
                // start next interval
                aggregated.add(currentInterval);
                LocalDateTime nextStart = currentInterval.endTime;
                currentInterval = new EnergyConsumption(nextStart, nextStart.plusMinutes(minutes), consumption.tariff, consumption.energy());
            }
        }
        if (!currentInterval.energy.equals(ZERO_ENERGY)) {
            aggregated.add(currentInterval);
        }
        return aggregated;
//        return consumptions;
    }

}
