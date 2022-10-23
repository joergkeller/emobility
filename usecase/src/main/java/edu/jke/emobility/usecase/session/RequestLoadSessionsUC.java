package edu.jke.emobility.usecase.session;

import edu.jke.emobility.domain.error.ApplicationException;
import edu.jke.emobility.domain.session.LoadSession;
import edu.jke.emobility.domain.session.SessionSummary;
import edu.jke.emobility.domain.tariff.SessionConsumption;
import edu.jke.emobility.domain.tariff.TariffSplitter;
import edu.jke.emobility.domain.value.CustomUnits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static edu.jke.emobility.domain.util.EnergyUtil.*;
import static edu.jke.emobility.domain.value.CustomUnits.WATT_HOUR;
import static javax.measure.MetricPrefix.KILO;

/**
 * Read the session in the time range from the load stations, write all sessions to one output file and summaries
 * per invoice term and station id.
 */
public class RequestLoadSessionsUC {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public record Request(List<String> stationNames, LocalDateTime from, LocalDateTime to) {}

    private static final Logger log = LoggerFactory.getLogger(RequestLoadSessionsUC.class);

    private final StationEndpoint stationAdapter;
    private final TariffSplitter tariffSplitter;
    private final WriterFactory writerFactory;

    /** Construct with adapter dependencies */
    public RequestLoadSessionsUC(StationEndpoint station, TariffSplitter tariffSplitter, WriterFactory writerFactory) {
        this.stationAdapter = station;
        this.tariffSplitter = tariffSplitter;
        this.writerFactory = writerFactory;
    }

    /** Validate a request */
    public Request validate(List<String> stationNames, LocalDateTime from, LocalDateTime to) throws ApplicationException {
        if (stationNames.isEmpty() || stationNames.stream().anyMatch(String::isBlank)) {
            throw new ApplicationException("Missing station name");
        }
        if (from.isAfter(to)) {
            throw new ApplicationException("Wrong time span (%s..%s)".formatted(from, to));
        }
        return new Request(stationNames, from, to);
    }

    public void invoke(Request request) {
        String sessionFileName = "Alle-Ladestationen-Ab-%tF".formatted(request.from());
        List<String> sessionFields = List.of("Owner", "StartTime", "EndTime", "Energy_kWh", "Basic_kWh", "Elevated_kWh");
        Function<SessionConsumption, List<String>> sessionMapper = consumption -> List.of(
                consumption.session().getChargerId().toString(),
                consumption.session().getChargingStart().format(DateTimeFormatter.ISO_DATE_TIME),
                consumption.session().getChargingEnd().format(DateTimeFormatter.ISO_DATE_TIME),
                String.valueOf(consumption.session().getEnergy().to(KILO(WATT_HOUR)).getValue()),
                String.valueOf(consumption.basicEnergy().to(KILO(WATT_HOUR)).getValue()),
                String.valueOf(consumption.elevatedEnergy().to(KILO(WATT_HOUR)).getValue())
        );

        String summaryFileName = "Zusammenfassung";
        List<String> summaryFields = List.of("Owner", "StartTime", "EndTime", "SessionCount", "Energy_kWh", "Basic_kWh", "Elevated_kWh");
        Function<SessionSummary, List<String>> summaryMapper = summary -> List.of(
                summary.chargerId().toString(),
                summary.start().format(DateTimeFormatter.ISO_DATE_TIME),
                summary.end().format(DateTimeFormatter.ISO_DATE_TIME),
                String.valueOf(summary.sessionCount()),
                String.valueOf(summary.energy().to(KILO(WATT_HOUR)).getValue()),
                String.valueOf(summary.basicEnergy().to(KILO(WATT_HOUR)).getValue()),
                String.valueOf(summary.elevatedEnergy().to(KILO(WATT_HOUR)).getValue())
        );

        try(
                OutputWriter<SessionConsumption> sessionWriter = writerFactory.createWriter(sessionFileName, sessionFields, sessionMapper);
                OutputWriter<SessionSummary> summaryWriter = writerFactory.createWriter(summaryFileName, summaryFields, summaryMapper);
        ) {
            request.stationNames().stream()
                    .map(name -> getImportSessions(request, name))
                    .peek(this::logSessions)
                    .peek(sessionWriter::write)
                    .map(this::sessionSummary)
                    .filter(SessionSummary::isValid)
                    .peek(this::logSummaries)
                    .forEach(summaryWriter::write);
        }
    }

    private List<SessionConsumption> getImportSessions(Request request, String name) {
        List<LoadSession> sessions = stationAdapter.importSessions(name, request.from(), request.to());

        List<SessionConsumption> consumptions = sessions.stream()
                .map(session -> stationAdapter.importProfile(name, session))
                .map(tariffSplitter::calculateConsumptions)
                .collect(Collectors.toList());

        stationAdapter.logout();

        return consumptions;
    }

    private void logSessions(List<SessionConsumption> consumptions) {
        consumptions.forEach(consumption ->
            log.info("Charging session at {} from {} to {} with {} (basic {}, elevated {})",
                    consumption.session().getChargerId(),
                    consumption.session().getChargingStart().format(formatter),
                    consumption.session().getChargingEnd().format(formatter),
                    format(consumption.session().getEnergy(), KILO(WATT_HOUR)),
                    format(consumption.basicEnergy(), KILO(WATT_HOUR)),
                    format(consumption.elevatedEnergy(), KILO(WATT_HOUR)))
        );
    }

    private SessionSummary sessionSummary(List<SessionConsumption> consumptions) {
        SessionSummary summary = new SessionSummary(null, null, null, 0, CustomUnits.ZERO_ENERGY, CustomUnits.ZERO_ENERGY, CustomUnits.ZERO_ENERGY);
        for (SessionConsumption consumption : consumptions) {
            summary = summary.add(consumption);
        }
        return summary;
    }

    private void logSummaries(SessionSummary summary) {
        log.info("Summary for {} from {} to {} is {} (basic {}, elevated {})",
                summary.chargerId(),
                summary.start().format(formatter),
                summary.end().format(formatter),
                format(summary.energy(), KILO(WATT_HOUR)),
                format(summary.basicEnergy(), KILO(WATT_HOUR)),
                format(summary.elevatedEnergy(), KILO(WATT_HOUR))
        );
    }

}
