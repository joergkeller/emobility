package edu.jke.emobility.domain.tariff;

import javax.measure.Quantity;
import javax.measure.quantity.Energy;
import java.time.LocalDateTime;

public record EnergyConsumption(
        LocalDateTime time,
        TariffSetting.Tariff tariff,
        Quantity<Energy> energy
) {}
