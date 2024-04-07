package com.improvetest.booking.service;

import com.improvetest.booking.dto.BookingNotificationDTO;

import java.util.List;

public interface NotificationService {

    void sendBookingInformation(BookingNotificationDTO bookingNotificationDTO);
    void sendBookingReminder(List<BookingNotificationDTO> bookingNotificationDTOS);

}
