package edu.jke.emobility.domain.value;

public class Energy {
    private final int amountWh;

    private Energy(int amountWh) {
        this.amountWh = amountWh;
    }

    public static Energy wh(int amountWh) {
        return new Energy(amountWh);
    }
}
