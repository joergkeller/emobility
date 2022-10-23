package edu.jke.emobility.adapter.station;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.ZonedDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProfileEntity(
        ZonedDateTime time,
        @JsonProperty("connector1")
        double connectorPower
) {}
