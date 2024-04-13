package com.improvetest.booking.service;

import com.improvetest.booking.model.Booking;
import com.improvetest.booking.model.Travel;
import com.improvetest.booking.repository.BookingRepository;
import com.improvetest.booking.repository.TravelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final TravelRepository travelRepository;

    @Override
    public Optional<Booking> getBookingById(String id) {
        return bookingRepository.findById(id);
    }

    @Override
    public Booking save(Booking booking) {
        booking.setId(UUID.randomUUID().toString());
        booking.setBookingNumber(UUID.randomUUID().toString());
        return bookingRepository.save(booking);
    }

    @Override
    public void deleteBooking(String bookingId) {
        bookingRepository.deleteById(bookingId);
    }

    @Override
    public List<Booking> getBookingWithDepartureDatesInLessThan(Duration durationInHour) {
        var now = LocalDateTime.now();
        var departureIn1hour = now.plusHours(durationInHour.toHours());

        var travelListWhichDepartureIsLessThan1Hour = travelRepository.findAllByDepartureDatetimeIsBetween(now, departureIn1hour);
        var travelIdList = travelListWhichDepartureIsLessThan1Hour.stream()
                .map(Travel::getId)
                .toList();

        return bookingRepository.findAllByTravelIdInAndIsRemindedFalse(travelIdList);
    }

    @Override
    public void isReminded(List<String> bookingIdList) {
        var bookings = bookingRepository.findAllByIdIn(bookingIdList);
        bookings.forEach(booking -> booking.setReminded(true));
        bookingRepository.saveAll(bookings);
    }
}
