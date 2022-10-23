package edu.jke.emobility.domain.value;

public class Price {

    private final double pricePerkWh;

    public Price(double pricePerkWh) {
        this.pricePerkWh = pricePerkWh;
    }

    public static Price perkWh(double pricePerkWh) {
        return new Price(pricePerkWh);
    }

}
