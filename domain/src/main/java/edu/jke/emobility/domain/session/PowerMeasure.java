package edu.jke.emobility.domain.session;

import javax.measure.Quantity;
import javax.measure.quantity.Power;
import java.time.LocalDateTime;

public record PowerMeasure(
        LocalDateTime time,
        Quantity<Power> power
) {}
