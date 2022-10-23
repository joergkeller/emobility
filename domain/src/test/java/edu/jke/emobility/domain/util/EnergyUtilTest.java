package edu.jke.emobility.domain.util;

import org.junit.jupiter.api.Test;
import tech.units.indriya.quantity.Quantities;
import tech.units.indriya.unit.Units;

import static edu.jke.emobility.domain.util.EnergyUtil.*;
import static edu.jke.emobility.domain.value.CustomUnits.WATT_HOUR;
import static javax.measure.MetricPrefix.KILO;
import static javax.measure.MetricPrefix.MEGA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class EnergyUtilTest {

    @Test
    void formatEnergy() {
        assertThat(format(Wh(12), WATT_HOUR)).isEqualTo("12.000 Wh");
        assertThat(format(Wh(12.98765), WATT_HOUR)).isEqualTo("12.988 Wh");
        assertThat(format(kWh(12.0), KILO(WATT_HOUR))).isEqualTo("12.000 kWh");
        assertThat(format(kWh(12000), MEGA(WATT_HOUR))).isEqualTo("12.000 MWh");
    }

    @Test
    void averageCalculation() {
        assertThat(average(Wh(5))).isEqualTo(Wh(5));
        assertThat(average(Wh(1), Wh(2), Wh(3))).isEqualTo(Wh(2));
    }

    @Test
    void averageCalculation_empty() {
        assertThatThrownBy(() -> average()).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void linearInterpolation_atStart() {
        assertThat(linearInterpolation(Wh(1), Wh(2), 100, 0)).isEqualTo(Wh(1));
    }

    @Test
    void linearInterpolation_atEnd() {
        assertThat(linearInterpolation(Wh(1), Wh(2), 100, 100)).isEqualTo(Wh(2));
    }

    @Test
    void linearInterpolation_inside() {
        assertThat(linearInterpolation(Wh(1), Wh(2), 100, 10).toString()).isEqualTo("1.1 Wh");
    }

    @Test
    void linearInterpolation_outside() {
        assertThat(linearInterpolation(Wh(1), Wh(2), 100, 200).toString()).isEqualTo("3 Wh");
    }

    @Test
    void energyCalculation() {
        assertThat(energyOf(Quantities.getQuantity(2, Units.WATT), time(30, Units.MINUTE)).to(WATT_HOUR)).isEqualTo(Wh(1));
    }
}
