package com.improvetest.booking.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "booking")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Booking {

    @Id
    private String id;
    private String bookingNumber;
    private String travelId;
    private String placeNumber;
    private String userId;
    private boolean isReminded;
}
