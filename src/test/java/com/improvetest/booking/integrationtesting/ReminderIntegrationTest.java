package com.improvetest.booking.integrationtesting;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.improvetest.booking.dto.BookingNotificationDTO;
import com.improvetest.booking.model.Booking;
import com.improvetest.booking.model.Travel;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.awaitility.Durations;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import java.time.LocalDateTime;
import java.util.List;

import static com.improvetest.booking.integrationtesting.initializer.KafkaInitializer.*;
import static com.improvetest.booking.integrationtesting.initializer.UserAccountMockServerInitializer.userAccountMockServer;
import static com.improvetest.booking.integrationtesting.utils.FileUtil.loadFileAsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
class ReminderIntegrationTest extends IntegrationTest {
    private static final String TRAVEL_ID_WILL_START_SOON_1 = "411d7482-1093-4976-ada0-ef902f337126";
    private static final String TRAVEL_ID_WILL_START_SOON_2 = "511d7482-1093-4976-ada0-ef902f337126";
    private static final String TRAVEL_ID_WILL_NOT_START_SOON = "74ac29dd-7d25-4823-b437-7170ba091360";

    private static final String TRAVEL_JSON = loadFileAsString("dataset/reminder/reminder-travel.json");
    private static final String BOOKING_JSON = loadFileAsString("dataset/reminder/reminder-booking.json");

    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Autowired
    private ObjectMapper objectMapper;
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
    public void given4BookingWith2MustBeReminded_WhenTriggerReminderProcess_2NotificationAreSent() throws Exception {
        // Given
        userAccountMockServer.addStubMapping(StubMapping.buildFrom(GET_USER_BY_ID_OK_MAPPING_JSON));
        saveBookingsForReminding();

        // When
        mockMvc.perform(post(INTERNAL_BOOKING_V1_API_BASE_PATH + "/trigger-reminder")).andExpect(status().isOk());

        // Then
        waitingForReminderProcessToBeEnded();
        var bookingNotificationDTOS = getRecordsFromKafka(bookingNotificationDTOConsumer, messagingTopic);
        assertEquals(2, bookingNotificationDTOS.size());
        var travelIds = bookingNotificationDTOS.stream().map(BookingNotificationDTO::getTravelId).toList();
        assertTrue(travelIds.contains(TRAVEL_ID_WILL_START_SOON_1));
        assertTrue(travelIds.contains(TRAVEL_ID_WILL_START_SOON_2));
    }

    private void saveBookingsForReminding() throws JsonProcessingException {

        var travelList = objectMapper.readValue(TRAVEL_JSON, new TypeReference<List<Travel>>() {});
        assertEquals(3, travelList.size());
        travelList.forEach(travel -> {
            if (TRAVEL_ID_WILL_START_SOON_1.equals(travel.getId())){
                travel.setDepartureDatetime(LocalDateTime.now().plusHours(1)); // start soon
            }
            if (TRAVEL_ID_WILL_START_SOON_2.equals(travel.getId())){
                travel.setDepartureDatetime(LocalDateTime.now().plusMinutes(5)); // start soon
            }
            if (TRAVEL_ID_WILL_NOT_START_SOON.equals(travel.getId())){
                travel.setDepartureDatetime(LocalDateTime.now().plusDays(3)); // not start soon
            }

        });
        travelRepository.saveAll(travelList);

        var relatedBookingList = objectMapper.readValue(BOOKING_JSON, new TypeReference<List<Booking>>() {});
        assertEquals(4, relatedBookingList.size());
        bookingRepository.saveAll(relatedBookingList);
    }

    private void waitingForReminderProcessToBeEnded() {
        Awaitility.await()
                .atMost(Durations.FIVE_SECONDS)
                .until(() -> threadPoolTaskExecutor.getThreadPoolExecutor().getActiveCount() == 0);
    }
}
