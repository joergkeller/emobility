package edu.jke.emobility.domain.tariff;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TariffSettingTest {

    @Test
    void findTariffChangeTimes_zeroInterval() {
        TariffSetting tariff = new TariffSetting();
        LocalDateTime now = LocalDateTime.now();
        assertThat(tariff.findTariffChangeTimes(now, now)).isEmpty();
    }

    @Test
    void findTariffChangeTimes_noChange() {
        TariffSetting tariff = new TariffSetting();
        LocalDateTime from = LocalDateTime.of(2022, 10, 7, 12, 00); // friday 12:00
        LocalDateTime to = LocalDateTime.of(2022, 10, 7, 14, 00); // friday 14:00
        assertThat(tariff.findTariffChangeTimes(from, to)).isEmpty();
    }

    @Test
    void findTariffChangeTimes_baseToElevated() {
        TariffSetting tariff = new TariffSetting();
        LocalDateTime from = LocalDateTime.of(2022, 10, 7, 6, 00); // friday 06:00
        LocalDateTime to = LocalDateTime.of(2022, 10, 7, 8, 00); // friday 08:00
        List<LocalDateTime> changeTimes = tariff.findTariffChangeTimes(from, to);
        assertThat(changeTimes.size()).isEqualTo(1);
        assertThat(changeTimes.get(0).getHour()).isEqualTo(7);
    }

    @Test
    void findTariffChangeTimes_elevatedToBase() {
        TariffSetting tariff = new TariffSetting();
        LocalDateTime from = LocalDateTime.of(2022, 10, 7, 12, 00); // friday 12:00
        LocalDateTime to = LocalDateTime.of(2022, 10, 7, 23, 00); // friday 23:00
        List<LocalDateTime> changeTimes = tariff.findTariffChangeTimes(from, to);
        assertThat(changeTimes.size()).isEqualTo(1);
        assertThat(changeTimes.get(0).getHour()).isEqualTo(20);
    }

    @Test
    void findTariffChangeTimes_baseToElevatedToBase() {
        TariffSetting tariff = new TariffSetting();
        LocalDateTime from = LocalDateTime.of(2022, 10, 7, 2, 00); // friday 02:00
        LocalDateTime to = LocalDateTime.of(2022, 10, 7, 23, 00); // friday 23:00
        List<LocalDateTime> changeTimes = tariff.findTariffChangeTimes(from, to);
        assertThat(changeTimes.size()).isEqualTo(2);
        assertThat(changeTimes.get(0).getHour()).isEqualTo(7);
        assertThat(changeTimes.get(1).getHour()).isEqualTo(20);
    }

    @Test
    void findTariffChangeTimes_elevatedToBaseToElevated() {
        TariffSetting tariff = new TariffSetting();
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
        TariffSetting tariff = new TariffSetting();
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
