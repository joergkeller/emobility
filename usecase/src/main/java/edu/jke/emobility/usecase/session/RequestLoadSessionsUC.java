package edu.jke.emobility.usecase.session;

import edu.jke.emobility.domain.error.ApplicationException;
import edu.jke.emobility.domain.session.LoadSession;
import edu.jke.emobility.domain.session.SessionSummary;
import edu.jke.emobility.domain.tariff.EnergyConsumption;
import edu.jke.emobility.domain.tariff.SessionConsumption;
import edu.jke.emobility.domain.tariff.TariffSetting;
import edu.jke.emobility.domain.tariff.TariffSplitter;
import edu.jke.emobility.domain.util.DateUtil;
import edu.jke.emobility.domain.util.EnergyUtil;
import edu.jke.emobility.domain.value.CustomUnits;
import edu.jke.emobility.usecase.error.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketTimeoutException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static edu.jke.emobility.domain.util.EnergyUtil.format;
import static edu.jke.emobility.domain.value.CustomUnits.WATT_HOUR;
import static javax.measure.MetricPrefix.KILO;
import static tech.units.indriya.unit.Units.WATT;

/**
 * Read the session in the time range from the load stations, write all sessions to one output file and summaries
 * per invoice term and station id.
 */
public class RequestLoadSessionsUC {

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

    private List<String> profileFields = List.of("Start", "End", "Messwert", "Einheit", "Tarifart");
    private Function<EnergyConsumption, List<String>> profileMapper = consumption -> List.of(
            DateUtil.format(consumption.startTime()),
            DateUtil.format(consumption.endTime()),
            EnergyUtil.formatNoUnit(consumption.energy(), KILO(WATT_HOUR)),
            "KWH",
            consumption.tariff() == TariffSetting.Tariff.BASIC ? "NT" : "HT"
    );

    private List<String> sessionFields = List.of("Owner", "StartTime", "EndTime", "Energy_kWh", "Basic_kWh", "Elevated_kWh", "MaxPower_kW", "Mode", "StopReason");
    private Function<SessionConsumption, List<String>> sessionMapper = consumption -> List.of(
            consumption.session().userIdentification().name(),
            DateUtil.format(consumption.session().chargingStart()),
            DateUtil.format(consumption.session().chargingEnd()),
            EnergyUtil.formatNoUnit(consumption.session().energy(), KILO(WATT_HOUR)),
            EnergyUtil.formatNoUnit(consumption.basicEnergy(), KILO(WATT_HOUR)),
            EnergyUtil.formatNoUnit(consumption.elevatedEnergy(), KILO(WATT_HOUR)),
            EnergyUtil.formatNoUnit(consumption.session().maxPower(), KILO(WATT)),
            withDefault(consumption.session().mode(), ""),
            withDefault(consumption.session().stopReason(), "")
    );

    private List<String> summaryFields = List.of("Owner", "StartTime", "EndTime", "SessionCount", "Energy_kWh", "Basic_kWh", "Elevated_kWh");
    private Function<SessionSummary, List<String>> summaryMapper = summary -> List.of(
            summary.userIdentification().toString(),
            DateUtil.format(summary.start()),
            DateUtil.format(summary.end()),
            String.valueOf(summary.sessionCount()),
            EnergyUtil.formatNoUnit(summary.energy(), KILO(WATT_HOUR)),
            EnergyUtil.formatNoUnit(summary.basicEnergy(), KILO(WATT_HOUR)),
            EnergyUtil.formatNoUnit(summary.elevatedEnergy(), KILO(WATT_HOUR))
    );

    public void loadSessions(Request request) {
        String sessionFileName = "Alle-Ladestationen-Ab-%tF".formatted(request.from());
        String summaryFileName = "Zusammenfassung-Ab-%tF".formatted(request.from());
        try(
            OutputWriter<SessionConsumption> sessionWriter = writerFactory.createWriter(sessionFileName, sessionFields, sessionMapper);
            OutputWriter<SessionSummary> summaryWriter = writerFactory.createWriter(summaryFileName, summaryFields, summaryMapper);
        ) {
            request.stationNames().stream()
                    .map(name -> importSessions(request, name))
                    .peek(this::logSessions)
                    .peek(sessionWriter::write)
                    .map(this::sessionSummary)
                    .filter(SessionSummary::isValid)
                    .peek(this::logSummaries)
                    .forEach(summaryWriter::write);
        }
    }

    private static String withDefault(String value, String defaultValue) {
        return value == null ? defaultValue : value;
    }

    private List<SessionConsumption> importSessions(Request request, String name) {
        String profileFileName = "Ladestation-%s-Ab-%tF".formatted(name, request.from());
        try(
            OutputWriter<EnergyConsumption> profileWriter = writerFactory.createWriter(profileFileName, profileFields, profileMapper);
        ) {
            List<LoadSession> sessions = stationAdapter.importSessions(name, request.from(), request.to());

            List<SessionConsumption> consumption = sessions.stream()
                    .filter(session -> session.isValid())
                    .map(session -> stationAdapter.importProfile(name, session))
                    .map(tariffSplitter::calculateConsumptions)
                    .toList();

            consumption.stream()
                    .sorted(SessionConsumption.BY_START)
                    .flatMap(session -> session.convertInterval(15).stream())
                    .forEach(profileWriter::write);

            return consumption;
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

    private int compareByStartTime(SessionConsumption first, SessionConsumption second) {
        return first.session().chargingStart().compareTo(second.session().chargingStart());
    }

    private void logSessions(List<SessionConsumption> consumptions) {
        consumptions.forEach(consumption ->
            log.info("Charging session at {} from {} to {} with {} (basic {}, elevated {})",
                    consumption.session().userIdentification(),
                    DateUtil.format(consumption.session().chargingStart()),
                    DateUtil.format(consumption.session().chargingEnd()),
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
                DateUtil.format(summary.start()),
                DateUtil.format(summary.end()),
                format(summary.energy(), KILO(WATT_HOUR)),
                format(summary.basicEnergy(), KILO(WATT_HOUR)),
                format(summary.elevatedEnergy(), KILO(WATT_HOUR))
        );
    }

}
