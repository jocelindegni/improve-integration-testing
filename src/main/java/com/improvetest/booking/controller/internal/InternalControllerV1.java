package com.improvetest.booking.controller.internal;

import com.improvetest.booking.controller.BookingModule;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/api/v1/bookings")
@RequiredArgsConstructor
public class InternalControllerV1 {

    private final BookingModule bookingModule;
    @PostMapping("/trigger-reminder")
    public ResponseEntity<String> triggerBookingReminder(){
        bookingModule.triggerBookingReminder();
        return ResponseEntity.ok("Reminder process in progress ...");
    }
}
