package edu.jke.emobility.adapter.station;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import edu.jke.emobility.adapter.util.DurationDeserializer;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SessionEntity {
    private final UserEntity user;
    private final long chargingSessionId;
    private final ZonedDateTime chargingStartedTime;
    private final ZonedDateTime chargingEndedTime;
    private final Duration timeSpentCharging;
    private final long meterValueStart;
    private final long meterValueEnd;
    private final double activeEnergyConsumed;

    public SessionEntity() {
        this(null, 0L, null, null, null, 0, 0, 0.0d);
    }

    public SessionEntity(
            UserEntity user,
            long sessionId,
            ZonedDateTime chargingStarted,
            ZonedDateTime chargingEnded,
            Duration chargingDuration,
            long meterValueStart,
            long meterValueEnd,
            double activeEnergyConsumed) {
        this.user = user;
        this.chargingSessionId = sessionId;
        this.chargingStartedTime = chargingStarted;
        this.chargingEndedTime = chargingEnded;
        this.timeSpentCharging = chargingDuration;
        this.meterValueStart = meterValueStart;
        this.meterValueEnd = meterValueEnd;
        this.activeEnergyConsumed = activeEnergyConsumed;
    }

    public UserEntity getUser() {
        return user;
    }

    public long getChargingSessionId() {
        return chargingSessionId;
    }

    public ZonedDateTime getChargingStartedTime() {
        return chargingStartedTime;
    }

    public ZonedDateTime getChargingEndedTime() {
        return chargingEndedTime;
    }

    @JsonDeserialize(using = DurationDeserializer.class)
    public Duration getTimeSpentCharging() {
        return timeSpentCharging;
    }

    public long getMeterValueStart() {
        return meterValueStart;
    }

    public long getMeterValueEnd() {
        return meterValueEnd;
    }

    public double getActiveEnergyConsumed() {
        return activeEnergyConsumed;
    }
}
