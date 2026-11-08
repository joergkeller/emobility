package edu.jke.emobility.domain.session;

import javax.measure.Quantity;
import javax.measure.quantity.Power;
import java.time.ZonedDateTime;

public record PowerMeasure(
        ZonedDateTime time,
        Quantity<Power> power
) {}
