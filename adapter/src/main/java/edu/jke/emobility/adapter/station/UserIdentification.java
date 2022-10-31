package edu.jke.emobility.adapter.station;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UserIdentification(
        String userIdentificationType,
        String identificationCode,
        String number
) {}
