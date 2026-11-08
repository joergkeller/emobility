package edu.jke.emobility.domain.tariff;

import edu.jke.emobility.domain.error.ConsistencyException;
import edu.jke.emobility.domain.session.PowerMeasure;
import edu.jke.emobility.domain.session.PowerProfile;

import java.time.ZonedDateTime;
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

    /**
     * Ensure that the power profile covers the complete time range by adding measures at the start and end times
     * if they are missing.
     */
    default List<PowerMeasure> complementProfile(List<PowerMeasure> powerProfile, ZonedDateTime startTime, ZonedDateTime endTime) {
        if (powerProfile.isEmpty()) throw new ConsistencyException("Missing power profile");

        ArrayList<PowerMeasure> fullProfile = new ArrayList<>(powerProfile);
        PowerMeasure firstMeasure = fullProfile.getFirst();
        if (firstMeasure.time().isAfter(startTime)) {
            fullProfile.addFirst(new PowerMeasure(startTime, firstMeasure.power()));
        }

        PowerMeasure lastMeasure = fullProfile.getLast();
        if (lastMeasure.time().isBefore(endTime)) {
            fullProfile.add(new PowerMeasure(endTime, lastMeasure.power()));
        }

        return fullProfile;
    }
}
