package com.improvetest.booking.integrationtesting;

import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.improvetest.booking.dto.BookingNotificationDTO;
import com.improvetest.booking.model.Booking;
import com.improvetest.booking.model.Travel;
import org.apache.kafka.clients.consumer.Consumer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static com.improvetest.booking.integrationtesting.initializer.KafkaInitializer.*;
import static com.improvetest.booking.integrationtesting.initializer.UserAccountMockServerInitializer.userAccountMockServer;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class BookingCancellationIntegrationTest extends IntegrationTest{


    private Consumer<String, BookingNotificationDTO> bookingNotificationDTOConsumer;

    @BeforeEach
    void subscribeToMessagingTopic() {
        bookingNotificationDTOConsumer = buildConsumerAndSubscribeTo(GROUP_ID, messagingTopic);
    }

    @AfterEach
    void close() {
        unsubscribe(bookingNotificationDTOConsumer);
    }


    @Test
    void GivenUserWithBooking_ThenCallDeleteEndpoint_UserBookingIsDeletedAndNotificationIsSent() throws Exception {

        // Given
        var bookingId = UUID.randomUUID().toString();
        initDatabase(bookingId);
        userAccountMockServer.addStubMapping(StubMapping.buildFrom(GET_USER_BY_ID_OK_MAPPING_JSON));

        // When
        mockMvc.perform(
                delete(BOOKING_V1_API_BASE_PATH + "/" + bookingId)
        ).andExpect(status().isOk());

        // Then
        assertEquals(0, bookingRepository.findAll().size());
        var bookingNotificationDTOS = getRecordsFromKafka(bookingNotificationDTOConsumer, messagingTopic);
        assertEquals(1, bookingNotificationDTOS.size());
        var bookingNotificationDTO = bookingNotificationDTOS.get(0);
        assertEquals("CANCELLED", bookingNotificationDTO.getType());
        assertEquals(TRAVEL_ID, bookingNotificationDTO.getTravelId());
        assertEquals(USER_EMAIL, bookingNotificationDTO.getUserEmail());
        assertEquals(USERNAME, bookingNotificationDTO.getUsername());

    }

    @Test
    void GivenUserWithBooking_ThenCallDeleteEndpointWithInvalidId_Http404IsReturned() throws Exception {

        // Given
        var bookingId = UUID.randomUUID().toString();
        initDatabase(bookingId);
        userAccountMockServer.addStubMapping(StubMapping.buildFrom(GET_USER_BY_ID_OK_MAPPING_JSON));

        // When then
        var INVALID_BOOKING_ID = "INVALID_BOOKING_ID";
        mockMvc.perform(
                delete(BOOKING_V1_API_BASE_PATH + INVALID_BOOKING_ID)
        ).andExpect(status().isNotFound());

        // Then
        assertEquals(1, bookingRepository.findAll().size());
        var bookingNotificationDTOS = getRecordsFromKafka(bookingNotificationDTOConsumer, messagingTopic);
        assertEquals(0, bookingNotificationDTOS.size());
    }

    private void initDatabase(String bookingId){
        var travel = new Travel();
        travel.setId(TRAVEL_ID);
        travelRepository.save(travel);

        var booking = new Booking();
        booking.setId(bookingId);
        booking.setTravelId(TRAVEL_ID);
        booking.setUserId(USER_ID);
        bookingRepository.save(booking);

    }
}
