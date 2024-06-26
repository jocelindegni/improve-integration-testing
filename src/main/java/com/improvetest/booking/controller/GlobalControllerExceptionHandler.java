package com.improvetest.booking.controller;

import com.improvetest.booking.exception.BookingNotFoundException;
import com.improvetest.booking.exception.ServerIsDownException;
import com.improvetest.booking.exception.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalControllerExceptionHandler {

    @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "User not found")
    @ExceptionHandler(UserNotFoundException.class)
    public void userNotFoundHandler(){}

    @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Booking not found")
    @ExceptionHandler(BookingNotFoundException.class)
    public void bookingNotFoundHandler(){}

    @ResponseStatus(value = HttpStatus.SERVICE_UNAVAILABLE, reason = "Service unavailable")
    @ExceptionHandler(ServerIsDownException.class)
    public void serverIsDownExceptionHandler(){}
}
