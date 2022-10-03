package edu.jke.emobility.adapter.station;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Collections;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
class SessionResponseEntity {
    private final int errorCode;
    private final String errorMessage;
    private final List<SessionEntity> content;
    private final PagingInfoEntity pagingInfo;

    public SessionResponseEntity() {
        this(0, null, Collections.emptyList(), null);
    }

    public SessionResponseEntity(int errorCode, String errorMessage, List<SessionEntity> content, PagingInfoEntity pagingInfo) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.content = content;
        this.pagingInfo = pagingInfo;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public List<SessionEntity> getContent() {
        return content;
    }

    public PagingInfoEntity getPagingInfo() {
        return pagingInfo;
    }
}
