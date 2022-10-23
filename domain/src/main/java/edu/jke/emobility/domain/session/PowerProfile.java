package edu.jke.emobility.domain.session;

import java.util.List;

public record PowerProfile(
        LoadSession session,
        List<PowerMeasure> measurements
) {}
