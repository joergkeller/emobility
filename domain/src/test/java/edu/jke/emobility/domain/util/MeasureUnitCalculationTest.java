package edu.jke.emobility.domain.util;

import org.junit.jupiter.api.Test;
import tech.units.indriya.AbstractUnit;
import tech.units.indriya.function.MultiplyConverter;
import tech.units.indriya.function.RationalNumber;
import tech.units.indriya.unit.TransformedUnit;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Length;
import javax.measure.quantity.Power;
import javax.measure.quantity.Time;
import java.util.stream.Stream;

import static javax.measure.MetricPrefix.*;
import static org.assertj.core.api.Assertions.assertThat;
import static tech.units.indriya.quantity.Quantities.getQuantity;
import static tech.units.indriya.unit.Units.*;

/**
 * Learn about javax.measure implementations, especially how to convert basic unit of energy "Joule" into
 * transformed electrical unit "Watt*hour".
 */
public class MeasureUnitCalculationTest {

    @Test
    void timeUnits() {
        Quantity<Time> oneHour = getQuantity(1, HOUR);
        assertThat(oneHour.getUnit()).isEqualTo(HOUR);
        assertThat(oneHour.to(SECOND).getUnit()).isEqualTo(SECOND);
        assertThat(oneHour.to(SECOND).getValue()).isEqualTo(3600);
    }

    @Test
    void powerUnits() {
        Quantity<Power> oneKiloWatt = getQuantity(1, KILO(WATT));
        assertThat(oneKiloWatt.getUnit()).isEqualTo(KILO(WATT));
        assertThat(oneKiloWatt.to(WATT).getValue()).isEqualTo(1000);
    }

    @Test
    void powerUnitSymbol() {
        assertThat(WATT.getSymbol()).isEqualTo("W");
        assertThat(KILO(WATT).toString()).isEqualTo("kW");
        assertThat(MEGA(WATT).toString()).isEqualTo("MW");
    }

    @Test
    void energyCalculation() {
        Quantity<Time> oneHour = getQuantity(1, HOUR);
        Quantity<Power> oneKiloWatt = getQuantity(1, KILO(WATT));
        Quantity<Energy> energy = oneKiloWatt.multiply(oneHour).asType(Energy.class);
        assertThat(energy.to(JOULE).getUnit()).isEqualTo(JOULE);
        assertThat(energy.to(JOULE).toString()).isEqualTo("3600000 J");
    }

    @Test
    void lengthCalculation() {
        assertThat(METRE.getSystemUnit()).isEqualTo(METRE);
        assertThat(((AbstractUnit<Length>) METRE).isSystemUnit()).isTrue();
        Unit<Length> INCH = new TransformedUnit<>("In", "Inch", METRE, MultiplyConverter.ofRational(RationalNumber.of(254, 10000)));

        assertThat(getQuantity(1, INCH).to(MILLI(METRE)).toString()).isEqualTo("25.4 mm");
        assertThat(getQuantity(25.4, MILLI(METRE)).to(INCH).toString()).isEqualTo("0.999999999999999999999999999999999936 In");
    }

    @Test
    void deriveUnit() {
        Unit<Energy> WATT_HOUR = new TransformedUnit<>("Wh", "Watt*hour", JOULE, MultiplyConverter.of(3600));

        assertThat(getQuantity(3600, JOULE).to(WATT_HOUR).toString()).isEqualTo("1 Wh");
        assertThat(getQuantity(1, WATT_HOUR).to(JOULE).toString()).isEqualTo("3600 J");
    }

    @Test
    void electricalEnergyCalculation() {
        Unit<Energy> WATT_HOUR = new TransformedUnit<>("Wh", "Watt*hour", JOULE, MultiplyConverter.of(3600));
        Quantity<Time> oneHour = getQuantity(1, HOUR);
        Quantity<Power> oneKiloWatt = getQuantity(1, KILO(WATT));
        Quantity<Energy> energy = oneKiloWatt.multiply(oneHour).asType(Energy.class).to(WATT_HOUR);
        assertThat(energy.toString()).isEqualTo("1000 Wh");
        assertThat(energy.to(KILO(WATT_HOUR)).toString()).isEqualTo("1 kWh");
    }

    @Test
    void streamingCalculations() {
        Unit<Energy> WATT_HOUR = new TransformedUnit<>("Wh", "Watt*hour", JOULE, MultiplyConverter.of(3600));
        Stream<Quantity<Power>> powerMeasures = Stream.of(getQuantity(500, WATT), getQuantity(1600, WATT));
        Quantity<Energy> energy = powerMeasures
                .map(p -> p.multiply(getQuantity(1, HOUR)).asType(Energy.class))
                .reduce(getQuantity(0, WATT_HOUR), Quantity::add);
        assertThat(energy.to(KILO(WATT_HOUR)).toString()).isEqualTo("2.1 kWh");
    }

}
