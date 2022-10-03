package edu.jke.emobility.adapter.station;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
class LoginResponseEntity {
    private final int errorCode;
    private final String errorMessage;
    private final String token;

    public LoginResponseEntity() {
        this(0, null, null);
    }

    public LoginResponseEntity(int errorCode, String errorMessage, String token) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.token = token;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getToken() {
        return token;
    }
}
