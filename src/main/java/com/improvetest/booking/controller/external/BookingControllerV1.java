package com.improvetest.booking.controller.external;

import com.improvetest.booking.controller.BookingModule;
import com.improvetest.booking.dto.BookingDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(("/api/v1/bookings"))
@RequiredArgsConstructor
public class BookingControllerV1 {
    private final BookingModule bookingModule;
    @PostMapping
    public ResponseEntity<BookingDTO> book(@RequestBody BookingDTO bookingDTO){
        var bookingSaved =  bookingModule.book(bookingDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookingSaved);
    }


}
