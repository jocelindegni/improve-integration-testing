package com.improvetest.booking.service;

import com.improvetest.booking.model.Booking;

import java.time.Duration;
import java.util.List;

public interface BookingService {

    Booking save(Booking booking);

    List<Booking> getBookingWithDepartureDatesInLessThan(Duration duration);

    void isReminded(List<String> bookingIdList);
}
