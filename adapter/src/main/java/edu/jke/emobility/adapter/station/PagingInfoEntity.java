package edu.jke.emobility.adapter.station;

import com.fasterxml.jackson.annotation.JsonProperty;

record PagingInfoEntity(
        @JsonProperty("numOfRows")
        int numberOfRows,
        int pageCount
) {}
