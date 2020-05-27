package com.example.HackerNews.Exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiException {
    private final String message;
    private final HttpStatus httpStatus;
    private final boolean success;

    public ApiException(String message, HttpStatus httpStatus, boolean success) {
        this.message = message;
        this.httpStatus = httpStatus;
        this.success = success;
    }
}
