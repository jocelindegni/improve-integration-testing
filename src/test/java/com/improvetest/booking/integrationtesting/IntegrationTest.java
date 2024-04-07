package com.improvetest.booking.integrationtesting;

import com.improvetest.booking.integrationtesting.initializer.KafkaInitializer;
import com.improvetest.booking.integrationtesting.initializer.MongoInitializer;
import com.improvetest.booking.integrationtesting.initializer.UserAccountMockServerInitializer;
import com.improvetest.booking.repository.BookingRepository;
import com.improvetest.booking.repository.TravelRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static com.improvetest.booking.integrationtesting.initializer.KafkaInitializer.resetTopicsOffsetToLastOffset;
import static com.improvetest.booking.integrationtesting.initializer.UserAccountMockServerInitializer.resetUserAccountMockServerRequest;
import static com.improvetest.booking.integrationtesting.utils.FileUtil.loadFileAsString;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ContextConfiguration(
        initializers = {KafkaInitializer.class, MongoInitializer.class, UserAccountMockServerInitializer.class}
)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Slf4j
class IntegrationTest {

    protected static final String USER_ID = "996af67b-5fc9-4e3c-ba97-c3ad8f01994a";
    protected static final String USERNAME = "Jocelin Degni";
    protected static final String USER_EMAIL = "jojo@mail.com";
    protected static final String TRAVEL_ID = "4355a128-69f6-4fba-9a39-efa6ec39c4d5";
    protected static final String PLACE_NUMBER = "20B";

    protected static final String BOOKING_V1_API_BASE_PATH = "/api/v1/bookings";
    protected static final String INTERNAL_BOOKING_V1_API_BASE_PATH = "/internal/api/v1/bookings";

    protected static final String GET_USER_BY_ID_OK_MAPPING_JSON = loadFileAsString("wiremock-mappings/get-user-by-id-OK.json");




    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected BookingRepository bookingRepository;
    @Autowired
    protected TravelRepository travelRepository;
    @Value("${kafka.topics.messaging}")
    protected String messagingTopic;
    protected static final String GROUP_ID = "test_group_id";



    @BeforeEach
    void globalCleanup() {
        log.info("Cleanup environment ....");
        resetTopicsOffsetToLastOffset(GROUP_ID, messagingTopic);
        resetUserAccountMockServerRequest();
        bookingRepository.deleteAll();
        travelRepository.deleteAll();
    }
}
