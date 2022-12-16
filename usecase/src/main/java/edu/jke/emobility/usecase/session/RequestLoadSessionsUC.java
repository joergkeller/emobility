package edu.jke.emobility.usecase.session;

import edu.jke.emobility.domain.error.ApplicationException;
import edu.jke.emobility.domain.session.LoadSession;
import edu.jke.emobility.domain.session.SessionSummary;
import edu.jke.emobility.domain.tariff.SessionConsumption;
import edu.jke.emobility.domain.tariff.TariffSplitter;
import edu.jke.emobility.domain.value.CustomUnits;
import edu.jke.emobility.usecase.error.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketTimeoutException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static edu.jke.emobility.domain.util.EnergyUtil.format;
import static edu.jke.emobility.domain.value.CustomUnits.WATT_HOUR;
import static javax.measure.MetricPrefix.KILO;
import static tech.units.indriya.unit.Units.WATT;

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
        List<String> sessionFields = List.of("Owner", "StartTime", "EndTime", "Energy_kWh", "Basic_kWh", "Elevated_kWh", "MaxPower_kW", "Mode", "StopReason");
        Function<SessionConsumption, List<String>> sessionMapper = consumption -> List.of(
                consumption.session().userIdentification().name(),
                consumption.session().chargingStart().format(DateTimeFormatter.ISO_DATE_TIME),
                consumption.session().chargingEnd().format(DateTimeFormatter.ISO_DATE_TIME),
                String.valueOf(consumption.session().energy().to(KILO(WATT_HOUR)).getValue()),
                String.valueOf(consumption.basicEnergy().to(KILO(WATT_HOUR)).getValue()),
                String.valueOf(consumption.elevatedEnergy().to(KILO(WATT_HOUR)).getValue()),
                String.valueOf(consumption.session().maxPower().to(KILO(WATT)).getValue()),
                consumption.session().mode(),
                consumption.session().stopReason()
        );

        String summaryFileName = "Zusammenfassung";
        List<String> summaryFields = List.of("Owner", "StartTime", "EndTime", "SessionCount", "Energy_kWh", "Basic_kWh", "Elevated_kWh");
        Function<SessionSummary, List<String>> summaryMapper = summary -> List.of(
                summary.userIdentification().toString(),
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
        try {
            List<LoadSession> sessions = stationAdapter.importSessions(name, request.from(), request.to());

            return sessions.stream()
                    .filter(session -> session.isValid())
                    .map(session -> stationAdapter.importProfile(name, session))
                    .map(tariffSplitter::calculateConsumptions)
                    .collect(Collectors.toList());
        } catch(ServiceException exception) {
            if (exception.getCause() instanceof SocketTimeoutException) {
                log.warn("Station {} is not available", name);
                return Collections.emptyList();
            } else {
                throw exception;
            }
        } finally {
            stationAdapter.logout();
        }

    }

    private void logSessions(List<SessionConsumption> consumptions) {
        consumptions.forEach(consumption ->
            log.info("Charging session at {} from {} to {} with {} (basic {}, elevated {})",
                    consumption.session().userIdentification(),
                    consumption.session().chargingStart().format(formatter),
                    consumption.session().chargingEnd().format(formatter),
                    format(consumption.session().energy(), KILO(WATT_HOUR)),
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
                summary.userIdentification(),
                summary.start().format(formatter),
                summary.end().format(formatter),
                format(summary.energy(), KILO(WATT_HOUR)),
                format(summary.basicEnergy(), KILO(WATT_HOUR)),
                format(summary.elevatedEnergy(), KILO(WATT_HOUR))
        );
    }

}
