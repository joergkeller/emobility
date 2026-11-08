package edu.jke.emobility.usecase.session;

import edu.jke.emobility.domain.session.LoadSession;
import edu.jke.emobility.domain.session.PowerProfile;

import java.time.ZonedDateTime;
import java.util.List;

public interface StationEndpoint {
    List<LoadSession> importSessions(String stationName, ZonedDateTime from, ZonedDateTime to);

    PowerProfile importProfile(String stationName, LoadSession session);

    void logout();
}
