package edu.jke.emobility.usecase.session;

import edu.jke.emobility.domain.util.EnergyUtil;
import edu.jke.emobility.domain.value.ChargerId;
import edu.jke.emobility.domain.value.LoadSessionId;
import org.junit.jupiter.api.Test;

import javax.measure.Quantity;
import javax.measure.quantity.Energy;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AddLoadSessionTest {

    @Test
    void addLoadSession() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = LocalDateTime.now().plus(1, ChronoUnit.HOURS);
        ChargerId chargerId = new ChargerId("1");
        Quantity<Energy> energy = EnergyUtil.Wh(1517);
        AddLoadSessionUseCase.Request request = new AddLoadSessionUseCase.Request(start, end, chargerId, energy);

        AddLoadSessionUseCase addLoadSession = new AddLoadSessionUseCase();
        LoadSessionId sessionId = addLoadSession.invoke(request);

        assertNotNull(sessionId);
    }



}
