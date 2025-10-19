package edu.jke.emobility.domain.tariff;

import edu.jke.emobility.domain.session.LoadSession;
import edu.jke.emobility.domain.session.PowerMeasure;
import edu.jke.emobility.domain.session.PowerProfile;
import edu.jke.emobility.domain.value.UserIdentification;
import org.junit.jupiter.api.Test;

import javax.measure.Quantity;
import javax.measure.quantity.Energy;
import java.time.LocalDateTime;
import java.util.List;

import static edu.jke.emobility.domain.value.CustomUnits.*;
import static javax.measure.MetricPrefix.KILO;
import static org.assertj.core.api.Assertions.assertThat;
import static tech.units.indriya.quantity.Quantities.getQuantity;
import static tech.units.indriya.unit.Units.WATT;

public class InterpolationTariffSplitterTest {

    private final TariffSetting tariff = new TariffSetting();
    private final InterpolationTariffSplitter splitter = new InterpolationTariffSplitter(tariff);

    /**
     * 00:00 - 5 kW
     * 01:00 - assume unchanged 5 kW
     * Elapsed time 1 h
     * Average power 5 kW
     * Energy 5 kWh
     */
    @Test
    void singleTariff_singleProfile() {
        LocalDateTime from = LocalDateTime.parse("2022-10-09T00:00:00");
        LocalDateTime to = LocalDateTime.parse("2022-10-09T01:00:00");
        PowerProfile profile = new PowerProfile(
                new LoadSession(from, to, new UserIdentification("tbd"), ZERO_ENERGY, ZERO_POWER, null, null),
                List.of(
                    new PowerMeasure(from, getQuantity(5, KILO(WATT)))
                ));

        List<EnergyConsumption> consumptions = splitter.calculateConsumptions(profile).detailConsumptions();
        assertThat(consumptions.size()).isEqualTo(1);
        assertThat(consumptions.get(0).tariff()).isEqualTo(TariffSetting.Tariff.BASIC);
        assertThat(consumptions.get(0).energy().to(KILO(WATT_HOUR)).toString()).isEqualTo("5 kWh");
    }

    /**
     * 23:00 - 5 kW (previous day)
     * 00:00 - Start measurement
     * 01:00 - End measurement
     * 02:00 - 5 kW
     * Elapsed time 1 h
     * Average power 5 kW
     * Energy 5 kWh
     */
    @Test
    void singleTariff_longProfile() {
        LocalDateTime from = LocalDateTime.parse("2022-10-09T00:00:00");
        LocalDateTime to   = LocalDateTime.parse("2022-10-09T01:00:00");
        PowerProfile profile = new PowerProfile(
                new LoadSession(from, to, new UserIdentification("tbd"), ZERO_ENERGY, ZERO_POWER, null, null),
                List.of(
                    new PowerMeasure(LocalDateTime.parse("2022-10-08T23:00:00"), getQuantity(5, KILO(WATT))),
                    new PowerMeasure(LocalDateTime.parse("2022-10-09T02:00:00"), getQuantity(5, KILO(WATT)))
                ));

        List<EnergyConsumption> consumptions = splitter.calculateConsumptions(profile).detailConsumptions();
        assertThat(consumptions.size()).isEqualTo(1);
        assertThat(consumptions.get(0).energy().to(KILO(WATT_HOUR)).toString()).isEqualTo("5 kWh");
    }

    /**
     * 00:00 - 6 kW
     * 01:00 - 4 kW
     * Elapsed time 1 h
     * Average power 5 kW
     * Energy 5 kWh
     */
    @Test
    void singleTariff_meanProfile() {
        LocalDateTime from = LocalDateTime.parse("2022-10-09T00:00:00");
        LocalDateTime to = LocalDateTime.parse("2022-10-09T01:00:00");
        PowerProfile profile = new PowerProfile(
                new LoadSession(from, to, new UserIdentification("tbd"), ZERO_ENERGY, ZERO_POWER, null, null),
                List.of(
                    new PowerMeasure(from, getQuantity(6, KILO(WATT))),
                    new PowerMeasure(to, getQuantity(4, KILO(WATT)))
                ));

        List<EnergyConsumption> consumptions = splitter.calculateConsumptions(profile).detailConsumptions();
        assertThat(consumptions.size()).isEqualTo(1);
        assertThat(consumptions.get(0).energy().to(KILO(WATT_HOUR)).toString()).isEqualTo("5 kWh");
    }

    /**
     * 00:00 - Start measurement - unknown power
     * 00:20 - 6 kW
     * 01:00 - End measurement - 3 kW
     * First period 1/3 h * assumed 6 kW = 2 kWh
     * Second period 2/3 h * avg. 4.5 kW = 3 kWh
     */
    @Test
    void singleTariff_changingProfile() {
        LocalDateTime from = LocalDateTime.parse("2022-10-09T00:00:00");
        LocalDateTime to = LocalDateTime.parse("2022-10-09T01:00:00");
        PowerProfile profile = new PowerProfile(
                new LoadSession(from, to, new UserIdentification("tbd"), ZERO_ENERGY, ZERO_POWER, null, null),
                List.of(
                    new PowerMeasure(LocalDateTime.parse("2022-10-09T00:20:00"), getQuantity(6, KILO(WATT))),
                    new PowerMeasure(LocalDateTime.parse("2022-10-09T01:00:00"), getQuantity(3, KILO(WATT)))
                ));

        List<EnergyConsumption> consumptions = splitter.calculateConsumptions(profile).detailConsumptions();
        assertThat(consumptions.size()).isEqualTo(2);
        assertThat(consumptions.get(0).energy().to(KILO(WATT_HOUR)).toString()).isEqualTo("2 kWh");
        assertThat(consumptions.get(1).energy().to(KILO(WATT_HOUR)).toString()).isEqualTo("3 kWh");
    }

    /**
     * Test calculation of energy for a full period between two power measurements.
     * 00:00 - 6 kW
     * 01:00 - 4 kW
     * Elapsed time 1 h
     * Average power 5 kW
     * Energy 5 kWh
     */
    @Test
    void calculateEnergy_fullPeriod() {
        PowerMeasure firstMeasure = new PowerMeasure(LocalDateTime.parse("2022-10-09T00:00:00"), getQuantity(6, KILO(WATT)));
        PowerMeasure secondMeasure = new PowerMeasure(LocalDateTime.parse("2022-10-09T01:00:00"), getQuantity(4, KILO(WATT)));
        Quantity<Energy> energy = splitter.calculateEnergy(firstMeasure, secondMeasure, firstMeasure.time(), secondMeasure.time());
        assertThat(energy.to(KILO(WATT_HOUR)).toString()).isEqualTo("5 kWh");
    }

    /**
     * Test calculation of energy for a partial period between two power measurements.
     * 00:00 - Start measurement - 6 kW
     * 00:30 - End measurement - Estimated power 5 kW
     * 01:00 - 4 kW
     * Elapsed time 0.5 h
     * Average power during measurement 5.5 kW
     */
    @Test
    void calculateEnergy_startPeriod() {
        PowerMeasure firstMeasure = new PowerMeasure(LocalDateTime.parse("2022-10-09T00:00:00"), getQuantity(6, KILO(WATT)));
        PowerMeasure secondMeasure = new PowerMeasure(LocalDateTime.parse("2022-10-09T01:00:00"), getQuantity(4, KILO(WATT)));
        LocalDateTime from = LocalDateTime.parse("2022-10-09T00:00:00");
        LocalDateTime to   = LocalDateTime.parse("2022-10-09T00:30:00");
        Quantity<Energy> energy = splitter.calculateEnergy(firstMeasure, secondMeasure, from, to);
        assertThat(energy.to(KILO(WATT_HOUR)).toString()).isEqualTo("2.75 kWh");
    }

    /**
     * Test calculation of energy for a partial period between two power measurements.
     * 00:00 - 6 kW
     * 00:30 - Start measurement - Estimated power 5 kW
     * 01:00 - End measurment - 4 kW
     * Elapsed time 0.5 h
     * Average power during measurement 4.5 kW
     */
    @Test
    void calculateEnergy_endPeriod() {
        PowerMeasure firstMeasure = new PowerMeasure(LocalDateTime.parse("2022-10-09T00:00:00"), getQuantity(6, KILO(WATT)));
        PowerMeasure secondMeasure = new PowerMeasure(LocalDateTime.parse("2022-10-09T01:00:00"), getQuantity(4, KILO(WATT)));
        LocalDateTime from = LocalDateTime.parse("2022-10-09T00:30:00");
        LocalDateTime to   = LocalDateTime.parse("2022-10-09T01:00:00");
        Quantity<Energy> energy = splitter.calculateEnergy(firstMeasure, secondMeasure, from, to);
        assertThat(energy.to(KILO(WATT_HOUR)).toString()).isEqualTo("2.25 kWh");
    }

    /**
     * Test calculation of energy for a partial period between two power measurements.
     * 00:00 - 6 kW
     * 00:15 - Start measurement - Estimated power 5.5 kW
     * 00:45 - End measurement - Estimated power 4.5 kW
     * 01:00 - 4 kW
     * Elapsed time 0.5 h
     * Average power during measurement 5 kW
     */
    @Test
    void calculateEnergy_partialPeriod() {
        PowerMeasure firstMeasure = new PowerMeasure(LocalDateTime.parse("2022-10-09T00:00:00"), getQuantity(6, KILO(WATT)));
        PowerMeasure secondMeasure = new PowerMeasure(LocalDateTime.parse("2022-10-09T01:00:00"), getQuantity(4, KILO(WATT)));
        LocalDateTime from = LocalDateTime.parse("2022-10-09T00:15:00");
        LocalDateTime to   = LocalDateTime.parse("2022-10-09T00:45:00");
        Quantity<Energy> energy = splitter.calculateEnergy(firstMeasure, secondMeasure, from, to);
        assertThat(energy.to(KILO(WATT_HOUR)).toString()).isEqualTo("2.5 kWh");
    }

}
