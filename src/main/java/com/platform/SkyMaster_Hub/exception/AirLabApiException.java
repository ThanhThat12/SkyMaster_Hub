package com.platform.SkyMaster_Hub.exception;

import lombok.Getter;

@Getter
public class AirLabApiException extends RuntimeException {
    private int statusCode = 500;
    
    public AirLabApiException(String message) {
        super(message);
    }
    
    public AirLabApiException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public AirLabApiException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }
}
