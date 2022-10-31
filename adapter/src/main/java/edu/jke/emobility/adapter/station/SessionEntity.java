package edu.jke.emobility.adapter.station;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import edu.jke.emobility.adapter.util.DurationDeserializer;

import java.time.Duration;
import java.time.ZonedDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
record SessionEntity(
        UserIdentification userIdentification,
        UserEntity user,
        long chargingSessionId,
        ZonedDateTime chargingStartedTime,
        ZonedDateTime chargingEndedTime,
        @JsonDeserialize(using = DurationDeserializer.class)
        Duration timeSpentCharging,
        long meterValueStart,
        long meterValueEnd,
        double activeEnergyConsumed,
        double maxSessionPower,
        String chargingMode,
        String chargingSessionStopReason
) {}
