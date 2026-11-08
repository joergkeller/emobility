package edu.jke.emobility.domain.tariff;

import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TariffSettingTest {

    private final TariffSetting tariff = new TariffSetting();

    @Test
    void validAt_weekday_elevated() {
        ZonedDateTime time = dateTimeOf(2022, 10, 7, 12, 0); // friday 12:00
        assertThat(tariff.validAt(time)).isEqualTo(TariffSetting.Tariff.ELEVATED);
    }

    @Test
    void validAt_saturday_basic() {
        ZonedDateTime time = dateTimeOf(2022, 10, 8, 12, 0); // saturday 12:00
        assertThat(tariff.validAt(time)).isEqualTo(TariffSetting.Tariff.BASIC);
    }

    @Test
    void validAt_sunday_basic() {
        ZonedDateTime time = dateTimeOf(2022, 10, 9, 12, 0); // sunday 12:00
        assertThat(tariff.validAt(time)).isEqualTo(TariffSetting.Tariff.BASIC);
    }

    @Test
    void validAt_justBeforeElevation_basic() {
        ZonedDateTime time = dateTimeOf(2022, 10, 7, 6, 59); // friday 06:59
        assertThat(tariff.validAt(time)).isEqualTo(TariffSetting.Tariff.BASIC);
    }

    @Test
    void validAt_justAtChangeToElevated_elevated() {
        ZonedDateTime time = dateTimeOf(2022, 10, 7, 7, 0); // friday 07:00
        assertThat(tariff.validAt(time)).isEqualTo(TariffSetting.Tariff.ELEVATED);
    }

    @Test
    void validAt_justAfterElevation_elevated() {
        ZonedDateTime time = dateTimeOf(2022, 10, 7, 7, 1); // friday 07:01
        assertThat(tariff.validAt(time)).isEqualTo(TariffSetting.Tariff.ELEVATED);
    }

    @Test
    void validAt_justBeforeChangeToReduced_basic() {
        ZonedDateTime time = dateTimeOf(2022, 10, 7, 19, 59); // friday 19:59
        assertThat(tariff.validAt(time)).isEqualTo(TariffSetting.Tariff.ELEVATED);
    }

    @Test
    void validAt_justAtChangeToReduced_basic() {
        ZonedDateTime time = dateTimeOf(2022, 10, 7, 20, 0); // friday 20:00
        assertThat(tariff.validAt(time)).isEqualTo(TariffSetting.Tariff.BASIC);
    }

    @Test
    void validAt_justAfterChangeToReduced_basic() {
        ZonedDateTime time = dateTimeOf(2022, 10, 7, 20, 1); // friday 20:01
        assertThat(tariff.validAt(time)).isEqualTo(TariffSetting.Tariff.BASIC);
    }

    @Test
    void findTariffChangeTimes_zeroInterval() {
        ZonedDateTime now = ZonedDateTime.now();;
        assertThat(tariff.findTariffChangeTimes(now, now)).isEmpty();
    }

    @Test
    void findTariffChangeTimes_noChange() {
        ZonedDateTime from = dateTimeOf(2022, 10, 7, 12, 0); // friday 12:00
        ZonedDateTime to = dateTimeOf(2022, 10, 7, 14, 0); // friday 14:00
        assertThat(tariff.findTariffChangeTimes(from, to)).isEmpty();
    }

    @Test
    void findTariffChangeTimes_baseToElevated() {
        ZonedDateTime from = dateTimeOf(2022, 10, 7, 6, 0); // friday 06:00
        ZonedDateTime to = dateTimeOf(2022, 10, 7, 8, 0); // friday 08:00
        List<ZonedDateTime> changeTimes = tariff.findTariffChangeTimes(from, to);
        assertThat(changeTimes.size()).isEqualTo(1);
        assertThat(changeTimes.getFirst().getHour()).isEqualTo(7);
    }

    @Test
    void findTariffChangeTimes_elevatedToBase() {
        ZonedDateTime from = dateTimeOf(2022, 10, 7, 12, 0); // friday 12:00
        ZonedDateTime to = dateTimeOf(2022, 10, 7, 23, 0); // friday 23:00
        List<ZonedDateTime> changeTimes = tariff.findTariffChangeTimes(from, to);
        assertThat(changeTimes.size()).isEqualTo(1);
        assertThat(changeTimes.getFirst().getHour()).isEqualTo(20);
    }

    @Test
    void findTariffChangeTimes_baseToElevatedToBase() {
        ZonedDateTime from = dateTimeOf(2022, 10, 7, 2, 0); // friday 02:00
        ZonedDateTime to = dateTimeOf(2022, 10, 7, 23, 0); // friday 23:00
        List<ZonedDateTime> changeTimes = tariff.findTariffChangeTimes(from, to);
        assertThat(changeTimes.size()).isEqualTo(2);
        assertThat(changeTimes.get(0).getHour()).isEqualTo(7);
        assertThat(changeTimes.get(1).getHour()).isEqualTo(20);
    }

    @Test
    void findTariffChangeTimes_elevatedToBaseToElevated() {
        ZonedDateTime from = dateTimeOf(2022, 10, 6, 12, 0); // thursday 12:00
        ZonedDateTime to = dateTimeOf(2022, 10, 8, 12, 0); // saturday 12:00
        List<ZonedDateTime> changeTimes = tariff.findTariffChangeTimes(from, to);
        assertThat(changeTimes.size()).isEqualTo(3);
        assertThat(changeTimes.get(0).getHour()).isEqualTo(20);
        assertThat(changeTimes.get(1).getHour()).isEqualTo(7);
        assertThat(changeTimes.get(2).getHour()).isEqualTo(20);
    }

    @Test
    void findTariffChangeTimes_multipleDays() {
        ZonedDateTime from = dateTimeOf(2022, 10, 6, 12, 0); // thursday 12:00
        ZonedDateTime to = dateTimeOf(2022, 10, 10, 22, 0); // monday 22:00
        List<ZonedDateTime> changeTimes = tariff.findTariffChangeTimes(from, to);
        assertThat(changeTimes.size()).isEqualTo(5);
        assertThat(changeTimes.get(0).getHour()).isEqualTo(20);
        assertThat(changeTimes.get(1).getHour()).isEqualTo(7);
        assertThat(changeTimes.get(2).getHour()).isEqualTo(20);
        assertThat(changeTimes.get(3).getHour()).isEqualTo(7);
        assertThat(changeTimes.get(4).getHour()).isEqualTo(20);
    }

    private static ZonedDateTime dateTimeOf(int year, int month, int day, int hour, int minute) {
        return ZonedDateTime.of(year, month, day, hour, minute, 0, 0, ZoneId.systemDefault());
    }

}
