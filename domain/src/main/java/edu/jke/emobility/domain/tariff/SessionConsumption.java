package edu.jke.emobility.domain.tariff;

import edu.jke.emobility.domain.session.LoadSession;

import javax.measure.Quantity;
import javax.measure.quantity.Energy;
import java.util.List;

public record SessionConsumption(
        LoadSession session,
        Quantity<Energy> basicEnergy,
        Quantity<Energy> elevatedEnergy,
        List<EnergyConsumption> detailConsumptions
) {}
