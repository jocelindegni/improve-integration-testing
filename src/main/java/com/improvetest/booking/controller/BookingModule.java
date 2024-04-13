package com.improvetest.booking.controller;

import com.improvetest.booking.dto.BookingDTO;
import com.improvetest.booking.dto.BookingNotificationDTO;
import com.improvetest.booking.dto.NotificationTypeDTO;
import com.improvetest.booking.exception.BookingNotFoundException;
import com.improvetest.booking.mapper.BookingMapper;
import com.improvetest.booking.model.Booking;
import com.improvetest.booking.service.BookingService;
import com.improvetest.booking.service.NotificationService;
import com.improvetest.booking.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Objects;

import static com.improvetest.booking.dto.NotificationTypeDTO.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingModule {

    private final BookingService bookingService;
    private final UserService userService;
    private final NotificationService notificationService;
    private final BookingMapper bookingMapper;

    @Transactional
    public BookingDTO book(BookingDTO bookingDTO){
        var bookingEntity = bookingService.save(bookingMapper.toEntity(bookingDTO));
        var bookingNotification = getBookingNotification(bookingEntity, CREATED);
        notificationService.sendBookingInformation(bookingNotification);

        return bookingMapper.toDto(bookingEntity);
    }

    public void deleteBooking(String bookingId){
        var bookingEntity = bookingService.getBookingById(bookingId)
                        .orElseThrow(BookingNotFoundException::new);
        var bookingNotification = getBookingNotification(bookingEntity, CANCELLED);
        notificationService.sendBookingInformation(bookingNotification);
        bookingService.deleteBooking(bookingId);
    }

    @Async
    public void triggerBookingReminder(){
        var bookingList = bookingService.getBookingWithDepartureDatesInLessThan(Duration.ofHours(3));
        var bookingIdList = new ArrayList<String>();
        var bookingNotificationList = bookingList.stream()
                .map(booking -> {
                    bookingIdList.add(booking.getId());
                    return getBookingNotification(booking, REMINDER);
                })
                .toList();

        log.info("{} booking found to be reminded", bookingList.size());
        notificationService.sendBookingReminder(bookingNotificationList);
        bookingService.isReminded(bookingIdList);
    }

    private BookingNotificationDTO getBookingNotification(Booking booking, NotificationTypeDTO type){
        var user = userService.getUserById(booking.getUserId());
        var bookingNotification = bookingMapper.toBookingNotification(booking, user);
        bookingNotification.setType(type.toString());
        return bookingNotification;
    }
}
