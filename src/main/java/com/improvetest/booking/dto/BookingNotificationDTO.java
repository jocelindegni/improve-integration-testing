package com.improvetest.booking.dto;

import lombok.Data;

@Data
public class BookingNotificationDTO {
    private String bookingNumber;
    private String travelId;
    private String placeNumber;
    private String userEmail;
    private String username;
}
