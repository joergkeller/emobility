package edu.jke.emobility.usecase.session;

import edu.jke.emobility.domain.session.LoadSession;

import java.time.LocalDateTime;
import java.util.List;

public interface StationEndpoint {
    List<LoadSession> importSessions(String stationName, LocalDateTime from, LocalDateTime to);
}
