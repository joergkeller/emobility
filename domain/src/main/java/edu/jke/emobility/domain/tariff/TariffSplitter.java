package edu.jke.emobility.domain.tariff;

import edu.jke.emobility.domain.error.ConsistencyException;
import edu.jke.emobility.domain.session.PowerMeasure;
import edu.jke.emobility.domain.session.PowerProfile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Splits a charging session into individual energy consumption periods using discrete power profile measurements.
 * This allows an approximation of the consumption in basic or elevated tariff settings.
 * Approximation, because the exact power consumption curve between measurements is not known. The total session energy
 * measurement is assumed to be exact.
 */
public interface TariffSplitter {

    SessionConsumption calculateConsumptions(PowerProfile powerProfile);

    SessionConsumption scaleConsumptionToSession(SessionConsumption consumption);

    default List<PowerMeasure> complementProfile(List<PowerMeasure> powerProfile, LocalDateTime startTime, LocalDateTime endTime) {
        if (powerProfile.isEmpty()) throw new ConsistencyException("Missing power profile");

        ArrayList<PowerMeasure> fullProfile = new ArrayList<>(powerProfile);
        PowerMeasure firstMeasure = fullProfile.get(0);
        if (firstMeasure.time().isAfter(startTime)) {
            fullProfile.add(0, new PowerMeasure(startTime, firstMeasure.power()));
        }

        PowerMeasure lastMeasure = fullProfile.get(fullProfile.size() - 1);
        if (lastMeasure.time().isBefore(endTime)) {
            fullProfile.add(new PowerMeasure(endTime, lastMeasure.power()));
        }

        return fullProfile;
    }
}
