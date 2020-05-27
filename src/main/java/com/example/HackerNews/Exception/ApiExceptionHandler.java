package com.example.HackerNews.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(value=ApiRequestException.class)
    public ResponseEntity<Object> handleApiRequestException(ApiRequestException ex){
           HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
           ApiException apiException =new ApiException(ex.getMessage(),httpStatus,false);
           return new ResponseEntity<>(apiException,httpStatus);
    }
}
