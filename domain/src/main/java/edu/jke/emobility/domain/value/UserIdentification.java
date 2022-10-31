package edu.jke.emobility.domain.value;

public record UserIdentification(
    String name,
    String identificationType,
    String rfid,
    String cardNumber
) {

    public UserIdentification() {
        this("Unknown", null, null, null);
    }

    public UserIdentification(String name) {
        this(name, null, null, null);
    }
}