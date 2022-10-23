package edu.jke.emobility.domain.tariff;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TariffSetting {

    public enum Tariff { BASIC, ELEVATED }

    private record ElevationTime(LocalTime from, LocalTime to) {
        public Stream<LocalDateTime> materialize(LocalDate date) {
            if (from != null && to != null) {
                return Stream.of(
                        LocalDateTime.of(date, from),
                        LocalDateTime.of(date, to));
            } else {
                return Stream.empty();
            }
        }

        public boolean isElevated(LocalTime instant) {
            if (from == null && to == null) return false;
            else if (from == null) return instant.isBefore(to);
            else if (to == null) return from.isBefore(instant);
            else return from.isBefore(instant) && instant.isBefore(to);
        }
    }

    private final Map<DayOfWeek, ElevationTime> elevationTimes = Map.of(
            DayOfWeek.MONDAY,    new ElevationTime(LocalTime.of(7, 0), LocalTime.of(20, 0)),
            DayOfWeek.TUESDAY,   new ElevationTime(LocalTime.of(7, 0), LocalTime.of(20, 0)),
            DayOfWeek.WEDNESDAY, new ElevationTime(LocalTime.of(7, 0), LocalTime.of(20, 0)),
            DayOfWeek.THURSDAY,  new ElevationTime(LocalTime.of(7, 0), LocalTime.of(20, 0)),
            DayOfWeek.FRIDAY,    new ElevationTime(LocalTime.of(7, 0), LocalTime.of(20, 0)),
            DayOfWeek.SATURDAY,  new ElevationTime(LocalTime.of(7, 0), LocalTime.of(13, 0)),
            DayOfWeek.SUNDAY,    new ElevationTime(null, null)
    );

    public List<LocalDateTime> findTariffChangeTimes(LocalDateTime from, LocalDateTime to) {
        return from.toLocalDate().datesUntil(to.toLocalDate().plusDays(1))
                .flatMap(this::elevationTimesForDate)
                .filter(changeTime -> isBetween(changeTime, from, to))
                .collect(Collectors.toList());
    }

    private Stream<LocalDateTime> elevationTimesForDate(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return elevationTimes.get(dayOfWeek).materialize(date);
    }

    private boolean isBetween(LocalDateTime change, LocalDateTime from, LocalDateTime to) {
        return from.isBefore(change) && change.isBefore(to);
    }

    public Tariff validAt(LocalDateTime instant) {
        ElevationTime elevationTime = elevationTimes.get(instant.getDayOfWeek());
        return elevationTime.isElevated(instant.toLocalTime()) ? Tariff.ELEVATED : Tariff.BASIC;
    }

}
