package edu.jke.emobility.domain.session;

import edu.jke.emobility.domain.error.ConsistencyException;
import edu.jke.emobility.domain.tariff.SessionConsumption;
import edu.jke.emobility.domain.util.DateUtil;
import edu.jke.emobility.domain.util.EnergyUtil;
import edu.jke.emobility.domain.value.UserIdentification;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;

import static edu.jke.emobility.domain.value.CustomUnits.ZERO_ENERGY;
import static edu.jke.emobility.domain.value.CustomUnits.ZERO_POWER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class SessionSummaryTest {

    @Test
    void validSession() {
        SessionSummary summary = new SessionSummary(new UserIdentification("dummy"), LocalDateTime.now(), LocalDateTime.MAX, 1, EnergyUtil.Wh(5), ZERO_ENERGY, ZERO_ENERGY);
        assertThat(summary.isValid()).isTrue();
    }

    @Test
    void invalidSession() {
        SessionSummary summary = new SessionSummary();
        assertThat(summary.isValid()).isFalse();
    }

    @Test
    void latestTimestamp() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime future = now.plus(12, ChronoUnit.HOURS);
        assertThat(DateUtil.latest(null, now)).isEqualTo(now);
        assertThat(DateUtil.latest(now, future)).isEqualTo(future);
        assertThat(DateUtil.latest(future, now)).isEqualTo(future);
        assertThatThrownBy(() -> DateUtil.latest(now, null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void earliestTimestamp() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime future = now.plus(12, ChronoUnit.HOURS);
        assertThat(DateUtil.earliest(null, now)).isEqualTo(now);
        assertThat(DateUtil.earliest(now, future)).isEqualTo(now);
        assertThat(DateUtil.earliest(future, now)).isEqualTo(now);
        assertThatThrownBy(
                () -> DateUtil.earliest(now, null)
        ).isInstanceOf(NullPointerException.class);
    }

    @Test
    void addDifferentCharger_throwsException() {
        SessionSummary summary = new SessionSummary(new UserIdentification("this"), null, null, 0, ZERO_ENERGY, ZERO_ENERGY, ZERO_ENERGY);
        assertThatThrownBy(
                () -> summary.add(new SessionConsumption(
                        new LoadSession(LocalDateTime.now(), LocalDateTime.now(), new UserIdentification("that"), EnergyUtil.Wh(5), ZERO_POWER, null, null),
                        null,
                        null,
                        null))
        ).isInstanceOf(ConsistencyException.class);
    }

    @Test
    void addSession_toAnEmptySummary() {
        SessionSummary empty = new SessionSummary();
        SessionSummary summary = empty.add(new SessionConsumption(
                new LoadSession(LocalDateTime.MIN, LocalDateTime.MAX, new UserIdentification("new"), EnergyUtil.Wh(555), ZERO_POWER, null, null),
                ZERO_ENERGY,
                ZERO_ENERGY,
                Collections.emptyList()));
        assertThat(summary.userIdentification().name()).isEqualTo("new");
        assertThat(summary.energy().toString()).isEqualTo("555 Wh");
        assertThat(summary.start()).isEqualTo(LocalDateTime.MIN);
        assertThat(summary.end()).isEqualTo(LocalDateTime.MAX);
    }

}
