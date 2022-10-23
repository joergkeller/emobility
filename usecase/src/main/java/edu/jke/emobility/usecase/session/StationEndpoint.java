package edu.jke.emobility.usecase.session;

import edu.jke.emobility.domain.session.LoadSession;
import edu.jke.emobility.domain.session.PowerProfile;

import java.time.LocalDateTime;
import java.util.List;

public interface StationEndpoint {
    List<LoadSession> importSessions(String stationName, LocalDateTime from, LocalDateTime to);

    PowerProfile importProfile(String stationName, LoadSession session);

    void logout();
}
