package com.improvetest.booking.repository;

import com.improvetest.booking.model.Booking;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface BookingRepository extends MongoRepository<Booking, String> {

    List<Booking> findAllByIdIn(List<String> idList);
    List<Booking> findAllByTravelIdInAndIsRemindedFalse(List<String> travelIdList);

}
