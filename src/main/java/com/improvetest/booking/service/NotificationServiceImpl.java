package com.improvetest.booking.service;

import com.improvetest.booking.dto.BookingNotificationDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    @Value("${kafka.topics.messaging}")
    private String messagingTopic;
    private final KafkaTemplate<String, BookingNotificationDTO> kafkaTemplate;

    @Override
    public void sendBookingInformation(BookingNotificationDTO bookingNotificationDTO) {
        log.info("Send booking notification...");
        kafkaTemplate.send(messagingTopic, bookingNotificationDTO);
    }

    @Override
    public void sendBookingReminder(List<BookingNotificationDTO> bookingNotificationDTOS) {
        log.info("Send reminder notifications...");
        bookingNotificationDTOS.forEach(bookingNotificationDTO -> kafkaTemplate.send(messagingTopic, bookingNotificationDTO));
    }
}
