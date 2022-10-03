package edu.jke.emobility.domain.value;

public class Energy {
    private final double amountWh;

    private Energy(double amountWh) {
        this.amountWh = amountWh;
    }

    public static Energy mWh(long start, long end) {
        return Wh((end - start) / 1000.0);
    }

    public static Energy Wh(long amountWh) {
        return new Energy(amountWh);
    }

    public static Energy Wh(double amountWh) {
        return new Energy(amountWh);
    }

    public static Energy kWh(long amountKWh) {
        return Wh(amountKWh * 1000);
    }

    public static Energy kWh(double amountKWh) {
        return Wh(amountKWh * 1000);
    }

    public Energy add(Energy summand) {
        return new Energy(amountWh + summand.amountWh);
    }

    public double asWh() {
        return amountWh;
    }

    public double askWh() {
        return amountWh / 1000;
    }

    @Override
    public String toString() {
        if (amountWh >= 10000) {
            return String.format("%.3f kWh", askWh());
        } else {
            return String.format("%.0f Wh", asWh());
        }
    }
}
