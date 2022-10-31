package edu.jke.emobility.domain.util;

import tech.units.indriya.quantity.Quantities;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Power;
import javax.measure.quantity.Time;
import java.util.stream.Stream;

import static edu.jke.emobility.domain.value.CustomUnits.WATT_HOUR;
import static javax.measure.MetricPrefix.KILO;
import static tech.units.indriya.quantity.Quantities.getQuantity;
import static tech.units.indriya.unit.Units.WATT;

public class EnergyUtil {

    public static Quantity<Power> W(Number amountW) {
        return getQuantity(amountW, WATT);
    }

    public static Quantity<Power> kW(Number amountkW) {
        return getQuantity(amountkW, KILO(WATT));
    }

    public static Quantity<Energy> Wh(Number amountWh) {
        return getQuantity(amountWh, WATT_HOUR);
    }

    public static Quantity<Energy> kWh(Number amountKWh) {
        return getQuantity(amountKWh, KILO(WATT_HOUR));
    }

    public static Quantity<Time> time(Number amount, Unit<Time> timeUnit) {
        return getQuantity(amount, timeUnit);
    }

    public static Quantity<Energy> energyOf(Quantity<Power> power, Quantity<Time> time) {
        return power.multiply(time).asType(Energy.class);
    }

    @SafeVarargs
    public static <U extends Quantity<U>> Quantity<U> average(Quantity<U>... values) {
        int count = values.length;
        if (count == 0) throw new IllegalArgumentException("Missing at least one value");
        return Stream.of(values)
                .reduce(Quantities.getQuantity(0, values[0].getUnit()), Quantity::add)
                .divide(count);
    }

    /**
     * Linear interpolation: v = v1 + (t-t1)(v2-v1)/(t2-t1)
     * Assuming t1 to be 0: v = v1 + t(v2-v1)/t2
     */
    public static <U extends Quantity<U>> Quantity<U> linearInterpolation(Quantity<U> first, Quantity<U> second, long timeBetween, long timeToInterpolation) {
        final Quantity<U> delta = second.subtract(first);
        return delta.multiply(timeToInterpolation).divide(timeBetween).add(first);
    }

    public static String format(Quantity<Energy> value, Unit<Energy> unit) {
        return String.format("%,.3f %s", value.to(unit).getValue().doubleValue(), unit.toString());
    }

}
