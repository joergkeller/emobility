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
import java.time.ZonedDateTime;
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

    /**
     * Calculate the energy consumption per tariff period based on discrete power profile measurements.
     */
    @Override
    public SessionConsumption calculateConsumptions(PowerProfile profile) {
        ZonedDateTime sessionStart = profile.session().chargingStart();
        ZonedDateTime sessionEnd = profile.session().chargingEnd();
        if (sessionEnd == null) sessionEnd = ZonedDateTime.now();
        List<ZonedDateTime> tariffChangeTimes = tariff.findTariffChangeTimes(sessionStart, sessionEnd);

        ArrayList<EnergyConsumption> consumptions = new ArrayList<>();
        Iterator<PowerMeasure> profileIterator = complementProfile(profile.measurements(), sessionStart, sessionEnd).iterator();
        ZonedDateTime startTime = sessionStart;
        PowerMeasure nextMeasure = profileIterator.next();
        while (nextMeasure.time().isBefore(sessionEnd)) {
            PowerMeasure lastMeasure = nextMeasure;
            nextMeasure = profileIterator.next();
            ZonedDateTime endTime = earliest(nextMeasure.time(), sessionEnd);
            Quantity<Energy> energy = calculateEnergy(lastMeasure, nextMeasure, startTime, endTime);
            consumptions.add(new EnergyConsumption(startTime, endTime, tariff.validAt(startTime), energy));
            startTime = endTime;
        }

        Map<TariffSetting.Tariff, Quantity<Energy>> tariffGroups = consumptions.stream()
                .collect(Collectors.toMap(EnergyConsumption::tariff, EnergyConsumption::energy, Quantity::add));

        Quantity<Energy> totalEnergy = tariffGroups.values().stream()
                .reduce(ZERO_ENERGY, Quantity::add)
                .to(WATT_HOUR);

        Quantity<Energy> sessionEnergy = profile.session().energy().to(WATT_HOUR);
        Quantity<Energy> delta = totalEnergy.subtract(sessionEnergy);

        return new SessionConsumption(
                profile.session(),
                tariffGroups.getOrDefault(TariffSetting.Tariff.BASIC, ZERO_ENERGY),
                tariffGroups.getOrDefault(TariffSetting.Tariff.ELEVATED, ZERO_ENERGY),
                consumptions);
    }

    /**
     * Scale the calculated consumption to the exact session energy.
     */
    @Override
    public SessionConsumption scaleConsumptionToSession(SessionConsumption consumption) {
        Quantity<Energy> totalEnergy = consumption.basicEnergy()
                .add(consumption.elevatedEnergy())
                .to(WATT_HOUR);

        Quantity<Energy> sessionEnergy = consumption.session().energy().to(WATT_HOUR);
        Number scalingFactor = Integer.valueOf(1);
        if (totalEnergy.getValue().doubleValue() > 0.0) scalingFactor = sessionEnergy.divide(totalEnergy).getValue();
        Quantity<Energy> delta = totalEnergy.subtract(sessionEnergy);
        log.info("Approximation of power consumption with error {}", EnergyUtil.format(delta, WATT_HOUR));

        return new SessionConsumption(
                consumption.session(),
                consumption.basicEnergy().multiply(scalingFactor),
                consumption.elevatedEnergy().multiply(scalingFactor),
                EnergyConsumption.multiply(consumption.detailConsumptions(), scalingFactor));
    }

    /**
     * Calculate the energy consumed between two power measurements over a given time interval.
     */
    Quantity<Energy> calculateEnergy(PowerMeasure first, PowerMeasure second, ZonedDateTime startTime, ZonedDateTime endTime) {
        if (first.time().isAfter(second.time())) throw new ConsistencyException("Power profiling is not in order (%s,  %s)".formatted(first.time(), second.time()));
        if (startTime.isAfter(endTime)) throw new ConsistencyException("Energy calculation is not in order (%s ->  %s)".formatted(startTime, endTime));
        if (startTime.isBefore(first.time())) throw new ConsistencyException("Energy calculation before first power measure (%s < %s)".formatted(startTime, first.time()));
        if (endTime.isAfter(second.time())) throw new ConsistencyException("Energy calculation after last power measure (%s > %s)".formatted(endTime, second.time()));

        Quantity<Power> averagePower = powerEstimation(first, second, startTime, endTime);
        Quantity<Time> duration = EnergyUtil.time(SECONDS.between(startTime, endTime), Units.SECOND);
        return EnergyUtil.energyOf(averagePower, duration);
    }

    /**
     * Assuming constant power (defined by the first measurement) during the whole cycle.
     */
    Quantity<Power> powerEstimation(PowerMeasure first, PowerMeasure second, ZonedDateTime startTime, ZonedDateTime endTime) {
        return first.power();
    }

}
