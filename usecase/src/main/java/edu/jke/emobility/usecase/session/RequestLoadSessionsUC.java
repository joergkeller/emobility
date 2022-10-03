package edu.jke.emobility.usecase.session;

import edu.jke.emobility.domain.session.LoadSession;
import edu.jke.emobility.domain.session.SessionSummary;
import edu.jke.emobility.domain.value.Energy;
import edu.jke.emobility.domain.error.ApplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Function;

/**
 * Read the session in the time range from the load stations, write all sessions to one output file and summaries
 * per invoice term and station id.
 */
public class RequestLoadSessionsUC {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public record Request(List<String> stationNames, LocalDateTime from, LocalDateTime to) {}

    private static Logger log = LoggerFactory.getLogger(RequestLoadSessionsUC.class);

    private final StationEndpoint stationAdapter;
    private final WriterFactory writerFactory;

    /** Construct with adapter dependencies */
    public RequestLoadSessionsUC(StationEndpoint station, WriterFactory writerFactory) {
        this.stationAdapter = station;
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
        List<String> sessionFields = List.of("Owner", "StartTime", "EndTime", "Energy_kWh");
        Function<LoadSession, List<String>> sessionMapper = session -> List.of(
                session.getChargerId().toString(),
                session.getChargingStart().format(DateTimeFormatter.ISO_DATE_TIME),
                session.getChargingEnd().format(DateTimeFormatter.ISO_DATE_TIME),
                String.valueOf(session.getEnergy().askWh())
        );

        String summaryFileName = "Zusammenfassung";
        List<String> summaryFields = List.of("Owner", "StartTime", "EndTime", "Energy_kWh");
        Function<SessionSummary, List<String>> summaryMapper = summary -> List.of(
                summary.chargerId().toString(),
                summary.start().format(DateTimeFormatter.ISO_DATE_TIME),
                summary.end().format(DateTimeFormatter.ISO_DATE_TIME),
                String.valueOf(summary.energy().askWh())
        );

        try(
                OutputWriter<LoadSession> sessionWriter = writerFactory.createWriter(sessionFileName, sessionFields, sessionMapper);
                OutputWriter<SessionSummary> summaryWriter = writerFactory.createWriter(summaryFileName, summaryFields, summaryMapper);
        ) {
            request.stationNames().stream()
                    .map(name -> stationAdapter.importSessions(name, request.from(), request.to()))
                    .peek(this::logSessions)
                    .peek(sessionWriter::write)
                    .map(this::sessionSummary)
                    .filter(SessionSummary::isValid)
                    .peek(this::logSummaries)
                    .forEach(summaryWriter::write);
        }
    }

    private void logSessions(List<LoadSession> sessions) {
        sessions.stream()
                .forEach(s -> log.info("Load session at {} {} with {}", s.getChargerId(), s.getChargingStart().format(formatter), s.getEnergy()));
    }

    private SessionSummary sessionSummary(List<LoadSession> sessions) {
        SessionSummary summary = new SessionSummary(null, null, null, Energy.Wh(0));
        for (LoadSession session : sessions) {
            summary = summary.add(session);
        }
        return summary;
    }

    private void logSummaries(SessionSummary summary) {
        log.info("Summary for {} from {} to {} is {}", summary.chargerId(), summary.start().format(formatter), summary.end().format(formatter), summary.energy());
    }

}
