package edu.jke.emobility.domain.value;

import tech.units.indriya.function.MultiplyConverter;
import tech.units.indriya.quantity.Quantities;
import tech.units.indriya.unit.TransformedUnit;
import tech.units.indriya.unit.Units;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.Energy;

public class CustomUnits {

    public static final Unit<Energy> WATT_HOUR = new TransformedUnit<>("Wh", "Watt*hour", Units.JOULE, MultiplyConverter.of(3600));

    public static final Quantity<Energy> ZERO_ENERGY = Quantities.getQuantity(0, WATT_HOUR);

}
