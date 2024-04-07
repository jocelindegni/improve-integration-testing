package com.improvetest.booking.repository;

import com.improvetest.booking.model.Travel;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TravelRepository extends MongoRepository<Travel, String> {

    List<Travel> findAllByDepartureDatetimeIsBetween(LocalDateTime min, LocalDateTime max);

}
