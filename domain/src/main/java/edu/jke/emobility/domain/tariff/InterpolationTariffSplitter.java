package edu.jke.emobility.domain.tariff;

import edu.jke.emobility.domain.session.PowerMeasure;
import edu.jke.emobility.domain.util.EnergyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.measure.Quantity;
import javax.measure.quantity.Power;
import java.time.LocalDateTime;

import static java.time.temporal.ChronoUnit.SECONDS;

public class InterpolationTariffSplitter extends ConstantTariffSplitter {

    private static final Logger log = LoggerFactory.getLogger(InterpolationTariffSplitter.class);

    public InterpolationTariffSplitter(TariffSetting tariff) {
        super(tariff);
    }

    /**
     * Assuming linear power adoption from first to second measurement during the whole cycle. So the average power
     * is the average of the interpolated values at start and end time respectively.
     */
    @Override
    Quantity<Power> powerEstimation(PowerMeasure first, PowerMeasure second, LocalDateTime startTime, LocalDateTime endTime) {
        long totalTime = SECONDS.between(first.time(), second.time());
        Quantity<Power> firstPower = first.power();
        Quantity<Power> secondPower = second.power();
        long timeToStart = SECONDS.between((first.time()), startTime);
        Quantity<Power> estimatedStartPower = EnergyUtil.linearInterpolation(firstPower, secondPower, totalTime, timeToStart);
        long timeToEnd = SECONDS.between((first.time()), endTime);
        Quantity<Power> estimatedEndPower = EnergyUtil.linearInterpolation(firstPower, secondPower, totalTime, timeToEnd);

        Quantity<Power> averagePower = EnergyUtil.average(estimatedStartPower, estimatedEndPower);
        return averagePower;
    }

}
