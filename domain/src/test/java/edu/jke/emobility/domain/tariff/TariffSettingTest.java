package edu.jke.emobility.domain.tariff;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TariffSettingTest {

    private final TariffSetting tariff = new TariffSetting();

    @Test
    void validAt_weekday_elevated() {
        LocalDateTime time = LocalDateTime.of(2022, 10, 7, 12, 00); // friday 12:00
        assertThat(tariff.validAt(time)).isEqualTo(TariffSetting.Tariff.ELEVATED);
    }

    @Test
    void validAt_saturday_basic() {
        LocalDateTime time = LocalDateTime.of(2022, 10, 8, 12, 00); // saturday 12:00
        assertThat(tariff.validAt(time)).isEqualTo(TariffSetting.Tariff.BASIC);
    }

    @Test
    void validAt_sunday_basic() {
        LocalDateTime time = LocalDateTime.of(2022, 10, 9, 12, 00); // sunday 12:00
        assertThat(tariff.validAt(time)).isEqualTo(TariffSetting.Tariff.BASIC);
    }

    @Test
    void validAt_justBeforeElevation_basic() {
        LocalDateTime time = LocalDateTime.of(2022, 10, 7, 6, 59); // friday 06:59
        assertThat(tariff.validAt(time)).isEqualTo(TariffSetting.Tariff.BASIC);
    }

    @Test
    void validAt_justAtChangeToElevated_elevated() {
        LocalDateTime time = LocalDateTime.of(2022, 10, 7, 7, 00); // friday 07:00
        assertThat(tariff.validAt(time)).isEqualTo(TariffSetting.Tariff.ELEVATED);
    }

    @Test
    void validAt_justAfterElevation_elevated() {
        LocalDateTime time = LocalDateTime.of(2022, 10, 7, 7, 01); // friday 07:01
        assertThat(tariff.validAt(time)).isEqualTo(TariffSetting.Tariff.ELEVATED);
    }

    @Test
    void validAt_justBeforeChangeToReduced_basic() {
        LocalDateTime time = LocalDateTime.of(2022, 10, 7, 19, 59); // friday 19:59
        assertThat(tariff.validAt(time)).isEqualTo(TariffSetting.Tariff.ELEVATED);
    }

    @Test
    void validAt_justAtChangeToReduced_basic() {
        LocalDateTime time = LocalDateTime.of(2022, 10, 7, 20, 00); // friday 20:00
        assertThat(tariff.validAt(time)).isEqualTo(TariffSetting.Tariff.BASIC);
    }

    @Test
    void validAt_justAfterChangeToReduced_basic() {
        LocalDateTime time = LocalDateTime.of(2022, 10, 7, 20, 01); // friday 20:01
        assertThat(tariff.validAt(time)).isEqualTo(TariffSetting.Tariff.BASIC);
    }

    @Test
    void findTariffChangeTimes_zeroInterval() {
        LocalDateTime now = LocalDateTime.now();
        assertThat(tariff.findTariffChangeTimes(now, now)).isEmpty();
    }

    @Test
    void findTariffChangeTimes_noChange() {
        LocalDateTime from = LocalDateTime.of(2022, 10, 7, 12, 00); // friday 12:00
        LocalDateTime to = LocalDateTime.of(2022, 10, 7, 14, 00); // friday 14:00
        assertThat(tariff.findTariffChangeTimes(from, to)).isEmpty();
    }

    @Test
    void findTariffChangeTimes_baseToElevated() {
        LocalDateTime from = LocalDateTime.of(2022, 10, 7, 6, 00); // friday 06:00
        LocalDateTime to = LocalDateTime.of(2022, 10, 7, 8, 00); // friday 08:00
        List<LocalDateTime> changeTimes = tariff.findTariffChangeTimes(from, to);
        assertThat(changeTimes.size()).isEqualTo(1);
        assertThat(changeTimes.get(0).getHour()).isEqualTo(7);
    }

    @Test
    void findTariffChangeTimes_elevatedToBase() {
        LocalDateTime from = LocalDateTime.of(2022, 10, 7, 12, 00); // friday 12:00
        LocalDateTime to = LocalDateTime.of(2022, 10, 7, 23, 00); // friday 23:00
        List<LocalDateTime> changeTimes = tariff.findTariffChangeTimes(from, to);
        assertThat(changeTimes.size()).isEqualTo(1);
        assertThat(changeTimes.get(0).getHour()).isEqualTo(20);
    }

    @Test
    void findTariffChangeTimes_baseToElevatedToBase() {
        LocalDateTime from = LocalDateTime.of(2022, 10, 7, 2, 00); // friday 02:00
        LocalDateTime to = LocalDateTime.of(2022, 10, 7, 23, 00); // friday 23:00
        List<LocalDateTime> changeTimes = tariff.findTariffChangeTimes(from, to);
        assertThat(changeTimes.size()).isEqualTo(2);
        assertThat(changeTimes.get(0).getHour()).isEqualTo(7);
        assertThat(changeTimes.get(1).getHour()).isEqualTo(20);
    }

    @Test
    void findTariffChangeTimes_elevatedToBaseToElevated() {
        LocalDateTime from = LocalDateTime.of(2022, 10, 6, 12, 00); // thursday 12:00
        LocalDateTime to = LocalDateTime.of(2022, 10, 8, 12, 00); // saturday 12:00
        List<LocalDateTime> changeTimes = tariff.findTariffChangeTimes(from, to);
        assertThat(changeTimes.size()).isEqualTo(3);
        assertThat(changeTimes.get(0).getHour()).isEqualTo(20);
        assertThat(changeTimes.get(1).getHour()).isEqualTo(7);
        assertThat(changeTimes.get(2).getHour()).isEqualTo(20);
    }

    @Test
    void findTariffChangeTimes_multipleDays() {
        LocalDateTime from = LocalDateTime.of(2022, 10, 6, 12, 00); // thursday 12:00
        LocalDateTime to = LocalDateTime.of(2022, 10, 10, 22, 00); // monday 22:00
        List<LocalDateTime> changeTimes = tariff.findTariffChangeTimes(from, to);
        assertThat(changeTimes.size()).isEqualTo(5);
        assertThat(changeTimes.get(0).getHour()).isEqualTo(20);
        assertThat(changeTimes.get(1).getHour()).isEqualTo(7);
        assertThat(changeTimes.get(2).getHour()).isEqualTo(20);
        assertThat(changeTimes.get(3).getHour()).isEqualTo(7);
        assertThat(changeTimes.get(4).getHour()).isEqualTo(20);
    }

}
