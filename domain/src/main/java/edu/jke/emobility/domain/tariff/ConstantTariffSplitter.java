package edu.jke.emobility.domain.tariff;

import edu.jke.emobility.domain.error.ConsistencyException;
import edu.jke.emobility.domain.session.PowerMeasure;
import edu.jke.emobility.domain.session.PowerProfile;
import edu.jke.emobility.domain.util.EnergyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.units.indriya.unit.Units;

import javax.measure.Quantity;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Power;
import javax.measure.quantity.Time;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static edu.jke.emobility.domain.util.DateUtil.earliest;
import static edu.jke.emobility.domain.value.CustomUnits.WATT_HOUR;
import static edu.jke.emobility.domain.value.CustomUnits.ZERO_ENERGY;
import static java.time.temporal.ChronoUnit.SECONDS;

public class ConstantTariffSplitter implements TariffSplitter {

    private static final Logger log = LoggerFactory.getLogger(ConstantTariffSplitter.class);

    private final TariffSetting tariff;

    public ConstantTariffSplitter(TariffSetting tariff) {
        this.tariff = tariff;
    }

    @Override
    public SessionConsumption calculateConsumptions(PowerProfile profile) {
        LocalDateTime start = profile.session().chargingStart();
        LocalDateTime end = profile.session().chargingEnd();
        List<LocalDateTime> tariffChangeTimes = tariff.findTariffChangeTimes(start, end);

        ArrayList<EnergyConsumption> consumptions = new ArrayList<>();
        Iterator<PowerMeasure> profileIterator = complementProfile(profile.measurements(), start, end).iterator();
        LocalDateTime startTime = start;
        PowerMeasure nextMeasure = profileIterator.next();
        while (nextMeasure.time().isBefore(end)) {
            PowerMeasure lastMeasure = nextMeasure;
            nextMeasure = profileIterator.next();
            LocalDateTime endTime = earliest(nextMeasure.time(), end);
            Quantity<Energy> energy = calculateEnergy(lastMeasure, nextMeasure, startTime, endTime);
            consumptions.add(new EnergyConsumption(startTime, tariff.validAt(startTime), energy));
            startTime = endTime;
        }

        Map<TariffSetting.Tariff, Quantity<Energy>> tariffGroups = consumptions.stream()
                .collect(Collectors.toMap(EnergyConsumption::tariff, EnergyConsumption::energy, Quantity::add));

        Quantity<Energy> totalEnergy = tariffGroups.values().stream()
                .reduce(ZERO_ENERGY, Quantity::add)
                .to(WATT_HOUR);

        Quantity<Energy> sessionEnergy = profile.session().energy().to(WATT_HOUR);
        Number scalingFactor = Integer.valueOf(1);
        if (totalEnergy.getValue().doubleValue() > 0.0) scalingFactor = sessionEnergy.divide(totalEnergy).getValue();
        Quantity<Energy> delta = totalEnergy.subtract(sessionEnergy);
        log.info("Approximation of power consumption with error {}", EnergyUtil.format(delta, WATT_HOUR));

        return new SessionConsumption(
                profile.session(),
                tariffGroups.getOrDefault(TariffSetting.Tariff.BASIC, ZERO_ENERGY).multiply(scalingFactor),
                tariffGroups.getOrDefault(TariffSetting.Tariff.ELEVATED, ZERO_ENERGY).multiply(scalingFactor),
                consumptions);
    }

    private Quantity<Energy> combineConsumptions() {
        return null;
    }

    private EnergyConsumption summmarizeConsumptions(Map.Entry<TariffSetting.Tariff, List<EnergyConsumption>> entry) {
        Quantity<Energy> totalEnergy = entry.getValue().stream()
                .map(EnergyConsumption::energy)
                .reduce(ZERO_ENERGY, Quantity::add);
        return new EnergyConsumption(entry.getValue().get(0).time(), entry.getKey(), totalEnergy);
    }

    Quantity<Energy> calculateEnergy(PowerMeasure first, PowerMeasure second, LocalDateTime startTime, LocalDateTime endTime) {
        if (first.time().isAfter(second.time())) throw new ConsistencyException("Power profiling is not in order");
        if (startTime.isAfter(endTime)) throw new ConsistencyException("Energy calculation is not in order");
        if (startTime.isBefore(first.time())) throw new ConsistencyException("Energy calculation before first power measure");
        if (endTime.isAfter(second.time())) throw new ConsistencyException("Energy calculation after last power measure");

        Quantity<Power> averagePower = powerEstimation(first, second, startTime, endTime);
        Quantity<Time> duration = EnergyUtil.time(SECONDS.between(startTime, endTime), Units.SECOND);
        return EnergyUtil.energyOf(averagePower, duration);
    }

    /**
     * Assuming constant power (defined by the first measurement) during the whole cycle.
     */
    Quantity<Power> powerEstimation(PowerMeasure first, PowerMeasure second, LocalDateTime startTime, LocalDateTime endTime) {
        return first.power();
    }

}
