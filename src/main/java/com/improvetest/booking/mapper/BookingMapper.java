package com.improvetest.booking.mapper;

import com.improvetest.booking.dto.BookingDTO;
import com.improvetest.booking.dto.BookingNotificationDTO;
import com.improvetest.booking.dto.UserDTO;
import com.improvetest.booking.model.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    BookingDTO toDto(Booking booking);

    @Mapping(target = "bookingNumber", ignore = true)
    Booking toEntity(BookingDTO bookingDto);

    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "user.email", target = "userEmail")
    BookingNotificationDTO toBookingNotification(Booking booking, UserDTO user);
}
