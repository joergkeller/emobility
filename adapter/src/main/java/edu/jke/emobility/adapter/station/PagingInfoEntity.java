package edu.jke.emobility.adapter.station;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PagingInfoEntity {
    private final int numberOfRows;
    private final int pageCount;

    public PagingInfoEntity() {
        this(0, 0);
    }

    public PagingInfoEntity(int numberOfRows, int pageCount) {
        this.numberOfRows = numberOfRows;
        this.pageCount = pageCount;
    }

    @JsonProperty("numOfRows")
    public int getNumberOfRows() {
        return numberOfRows;
    }

    public int getPageCount() {
        return pageCount;
    }
}
