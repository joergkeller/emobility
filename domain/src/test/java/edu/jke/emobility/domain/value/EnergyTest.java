package edu.jke.emobility.domain.value;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class EnergyTest {

    @Test
    void energy_Wh() {
        assertThat(Energy.Wh(12).asWh()).isEqualTo(12.0d);
    }

    @Test
    void energy_Wh_toString() {
        assertThat(Energy.Wh(12).toString()).isEqualTo("12 Wh");
        assertThat(Energy.Wh(12.5).toString()).isEqualTo("13 Wh");
    }

    @Test
    void energy_kWh() {
        assertThat(Energy.kWh(9.5).askWh()).isEqualTo(9.5d);
    }

    @Test
    void energy_kWh_toString() {
        assertThat(Energy.kWh(12).toString()).isEqualTo("12.000 kWh");
        assertThat(Energy.kWh(9.5).toString()).isEqualTo("9500 Wh");
    }

    @Test
    void energy_mWhCounter() {
        assertThat(Energy.mWh(1000, 13000).asWh()).isEqualTo(12.0d);
    }

    @Test
    void energy_mWhCounter_toString() {
        assertThat(Energy.mWh(1000, 13000).toString()).isEqualTo("12 Wh");
    }

}
