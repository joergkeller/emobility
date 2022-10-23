package edu.jke.emobility.domain.tariff;

import edu.jke.emobility.domain.session.LoadSession;
import edu.jke.emobility.domain.session.PowerMeasure;
import edu.jke.emobility.domain.session.PowerProfile;
import edu.jke.emobility.domain.util.EnergyUtil;
import edu.jke.emobility.domain.value.ChargerId;
import org.junit.jupiter.api.Test;

import javax.measure.Quantity;
import javax.measure.quantity.Energy;
import java.time.LocalDateTime;
import java.util.List;

import static edu.jke.emobility.domain.value.CustomUnits.WATT_HOUR;
import static javax.measure.MetricPrefix.KILO;
import static org.assertj.core.api.Assertions.assertThat;
import static tech.units.indriya.quantity.Quantities.getQuantity;
import static tech.units.indriya.unit.Units.WATT;

public class InterpolationTariffSplitterTest {

    private final TariffSetting tariff = new TariffSetting();
    private final InterpolationTariffSplitter splitter = new InterpolationTariffSplitter(tariff);

    @Test
    void singleTariff_singleProfile() {
        LocalDateTime from = LocalDateTime.parse("2022-10-09T00:00:00");
        LocalDateTime to = LocalDateTime.parse("2022-10-09T01:00:00");
        PowerProfile profile = new PowerProfile(new LoadSession(from, to, new ChargerId("tbd"), EnergyUtil.Wh(0)), List.of(
                new PowerMeasure(from, getQuantity(5, KILO(WATT)))));

        List<EnergyConsumption> consumptions = splitter.calculateConsumptions(profile).detailConsumptions();
        assertThat(consumptions.size()).isEqualTo(1);
        assertThat(consumptions.get(0).tariff()).isEqualTo(TariffSetting.Tariff.BASIC);
        assertThat(consumptions.get(0).energy().to(KILO(WATT_HOUR)).toString()).isEqualTo("5 kWh");
    }

    @Test
    void singleTariff_longProfile() {
        LocalDateTime from = LocalDateTime.parse("2022-10-09T00:00:00");
        LocalDateTime to   = LocalDateTime.parse("2022-10-09T01:00:00");
        PowerProfile profile = new PowerProfile(new LoadSession(from, to, new ChargerId("tbd"), EnergyUtil.Wh(0)), List.of(
                new PowerMeasure(LocalDateTime.parse("2022-10-08T23:00:00"), getQuantity(5, KILO(WATT))),
                new PowerMeasure(LocalDateTime.parse("2022-10-09T02:00:00"), getQuantity(5, KILO(WATT)))));

        List<EnergyConsumption> consumptions = splitter.calculateConsumptions(profile).detailConsumptions();
        assertThat(consumptions.size()).isEqualTo(1);
        assertThat(consumptions.get(0).energy().to(KILO(WATT_HOUR)).toString()).isEqualTo("5 kWh");
    }

    @Test
    void singleTariff_meanProfile() {
        LocalDateTime from = LocalDateTime.parse("2022-10-09T00:00:00");
        LocalDateTime to = LocalDateTime.parse("2022-10-09T01:00:00");
        PowerProfile profile = new PowerProfile(new LoadSession(from, to, new ChargerId("tbd"), EnergyUtil.Wh(0)), List.of(
                new PowerMeasure(from, getQuantity(6, KILO(WATT))),
                new PowerMeasure(to, getQuantity(4, KILO(WATT)))));

        List<EnergyConsumption> consumptions = splitter.calculateConsumptions(profile).detailConsumptions();
        assertThat(consumptions.size()).isEqualTo(1);
        assertThat(consumptions.get(0).energy().to(KILO(WATT_HOUR)).toString()).isEqualTo("5 kWh");
    }

    @Test
    void singleTariff_changingProfile() {
        LocalDateTime from = LocalDateTime.parse("2022-10-09T00:00:00");
        LocalDateTime to = LocalDateTime.parse("2022-10-09T01:00:00");
        PowerProfile profile = new PowerProfile(new LoadSession(from, to, new ChargerId("tbd"), EnergyUtil.Wh(0)), List.of(
                new PowerMeasure(LocalDateTime.parse("2022-10-09T00:20:00"), getQuantity(6, KILO(WATT))),
                new PowerMeasure(LocalDateTime.parse("2022-10-09T01:00:00"), getQuantity(3, KILO(WATT)))));

        List<EnergyConsumption> consumptions = splitter.calculateConsumptions(profile).detailConsumptions();
        assertThat(consumptions.size()).isEqualTo(2);
        assertThat(consumptions.get(0).energy().to(KILO(WATT_HOUR)).toString()).isEqualTo("2 kWh");
        assertThat(consumptions.get(1).energy().to(KILO(WATT_HOUR)).toString()).isEqualTo("3 kWh");
    }

    @Test
    void calculateEnergy_fullPeriod() {
        PowerMeasure firstMeasure = new PowerMeasure(LocalDateTime.parse("2022-10-09T00:00:00"), getQuantity(6, KILO(WATT)));
        PowerMeasure secondMeasure = new PowerMeasure(LocalDateTime.parse("2022-10-09T01:00:00"), getQuantity(4, KILO(WATT)));
        Quantity<Energy> energy = splitter.calculateEnergy(firstMeasure, secondMeasure, firstMeasure.time(), secondMeasure.time());
        assertThat(energy.to(KILO(WATT_HOUR)).toString()).isEqualTo("5 kWh");
    }

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
