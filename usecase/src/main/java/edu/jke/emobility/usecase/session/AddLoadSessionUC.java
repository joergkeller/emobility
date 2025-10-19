package edu.jke.emobility.usecase.session;

import edu.jke.emobility.domain.session.LoadSession;
import edu.jke.emobility.domain.util.EnergyUtil;
import edu.jke.emobility.domain.value.LoadSessionId;
import edu.jke.emobility.domain.value.UserIdentification;

import javax.measure.Quantity;
import javax.measure.quantity.Energy;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static edu.jke.emobility.domain.value.CustomUnits.ZERO_POWER;

public class AddLoadSessionUC {
    public record Request(LocalDateTime start, LocalDateTime end, UserIdentification userIdentification, Quantity<Energy> energy) {}

    public static Request validate(String start, String end, String chargerId, String energy) {
        // TODO create Try object (TryValidation)
        return new Request(
                LocalDateTime.parse(start, DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                LocalDateTime.parse(end, DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                new UserIdentification(chargerId),
                EnergyUtil.Wh(Integer.parseInt(energy))
        );
    }

    public LoadSessionId invoke(Request input) {
        LoadSession session = new LoadSession(input.start, input.end, input.userIdentification, input.energy, ZERO_POWER, null, null);
        return session.sessionId();
    }

}
