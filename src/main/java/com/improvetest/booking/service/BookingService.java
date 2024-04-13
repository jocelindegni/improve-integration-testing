package com.improvetest.booking.service;

import com.improvetest.booking.model.Booking;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

public interface BookingService {

    Optional<Booking> getBookingById(String id);

    Booking save(Booking booking);

    void deleteBooking(String bookingId);

    List<Booking> getBookingWithDepartureDatesInLessThan(Duration duration);

    void isReminded(List<String> bookingIdList);
}
