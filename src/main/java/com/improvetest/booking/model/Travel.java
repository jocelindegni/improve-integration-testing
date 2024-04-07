package com.improvetest.booking.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "travel")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Travel {
    @Id
    private String id;
    private String departureCity;
    private String arrivalCity;
    private LocalDateTime departureDatetime;
}
