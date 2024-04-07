package com.improvetest.booking.integrationtesting;

import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.improvetest.booking.dto.BookingNotificationDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.improvetest.booking.integrationtesting.initializer.KafkaInitializer.*;
import static com.improvetest.booking.integrationtesting.initializer.UserAccountMockServerInitializer.userAccountMockServer;
import static com.improvetest.booking.integrationtesting.utils.FileUtil.loadFileAsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
class BookingIntegrationTest extends IntegrationTest {

    protected static final String BOOKING_WITH_GOOD_USER_ID = loadFileAsString("dataset/booking/booking-with-good-user-id.json");
    protected static final String BOOKING_WITH_BAD_USER_ID = loadFileAsString("dataset/booking/booking-with-bad-user-id.json");
    protected static final String GET_USER_BY_ID_503_MAPPING_JSON = loadFileAsString("wiremock-mappings/user-account-server-is-down.json");

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
    void givenValidBookingRequest_whenProcessIt_thenBookingIsSavedWithSuccess() throws Exception {

        // Given
        userAccountMockServer.addStubMapping(StubMapping.buildFrom(GET_USER_BY_ID_OK_MAPPING_JSON));

        // When then
        mockMvc.perform(
                        post(BOOKING_V1_API_BASE_PATH)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(BOOKING_WITH_GOOD_USER_ID)
                ).andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.bookingNumber").isNotEmpty())
                .andExpect(jsonPath("$.placeNumber").value(PLACE_NUMBER))
                .andExpect(jsonPath("$.travelId").value(TRAVEL_ID))
                .andExpect(jsonPath("$.userId").value(USER_ID));

        var bookingsInRepository = bookingRepository.findAll();
        assertNotNull(bookingsInRepository);
        assertEquals(1, bookingsInRepository.size());
        var bookingSaved = bookingsInRepository.get(0);
        assertNotNull(PLACE_NUMBER, bookingSaved.getBookingNumber());
        assertNotNull(TRAVEL_ID, bookingSaved.getTravelId());
        assertNotNull(USER_ID, bookingSaved.getUserId());

        var bookingNotificationDTOS = getRecordsFromKafka(bookingNotificationDTOConsumer, messagingTopic);
        assertEquals(1, bookingNotificationDTOS.size());
        var bookingNotificationDTO = bookingNotificationDTOS.get(0);
        assertNotNull(bookingNotificationDTO.getBookingNumber());
        assertEquals(PLACE_NUMBER, bookingNotificationDTO.getPlaceNumber());
        assertEquals(TRAVEL_ID, bookingNotificationDTO.getTravelId());
        assertEquals(USER_EMAIL, bookingNotificationDTO.getUserEmail());
        assertEquals(USERNAME, bookingNotificationDTO.getUsername());
    }

    @Test
    void givenBookingWithValidUserId_whenUserServerIsDown_ThenReturnHttp503() throws Exception {
        // Given
        userAccountMockServer.addStubMapping(StubMapping.buildFrom(GET_USER_BY_ID_503_MAPPING_JSON));

        // When then
        mockMvc.perform(
                post(BOOKING_V1_API_BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BOOKING_WITH_GOOD_USER_ID)
        ).andExpect(status().isServiceUnavailable());

        var bookingNotificationDTOS = getRecordsFromKafka(bookingNotificationDTOConsumer, messagingTopic);
        assertEquals(0, bookingNotificationDTOS.size());
        userAccountMockServer.verify(3, getRequestedFor(urlEqualTo("/internal/api/v1/users/" + USER_ID)));
    }

    @Test
    void givenBookingWithInvalidUserId_whenProcessIt_thenBookingIsSavedWithSuccess() throws Exception {
        // Given
        userAccountMockServer.addStubMapping(StubMapping.buildFrom(GET_USER_BY_ID_OK_MAPPING_JSON));

        // When then
        mockMvc.perform(
                post(BOOKING_V1_API_BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BOOKING_WITH_BAD_USER_ID)
        ).andExpect(status().isNotFound());

        // When then
        var bookingNotificationDTOS = getRecordsFromKafka(bookingNotificationDTOConsumer, messagingTopic);
        assertEquals(0, bookingNotificationDTOS.size());

    }
}
