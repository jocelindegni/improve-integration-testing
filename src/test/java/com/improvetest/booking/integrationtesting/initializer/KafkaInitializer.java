package com.improvetest.booking.integrationtesting.initializer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.shaded.org.awaitility.Durations;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.*;

@Slf4j
public class KafkaInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    public static final KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:5.4.7"))
            .withTmpFs(Map.of("/var/lib/kafka/data", "r"))
            .withTmpFs(Map.of("/etc/kafka/secrets", "rw"));

    static {
        kafkaContainer.start();
    }

    @Override
    public void initialize(@NotNull ConfigurableApplicationContext applicationContext) {
        TestPropertyValues
                .of(Map.of("spring.kafka.bootstrap-servers", kafkaContainer.getBootstrapServers()))
                .applyTo(applicationContext);
    }

    public static <T> Consumer<String, T> buildConsumerAndSubscribeTo(String groupId, String... topics) {
        log.info("Subscription to topic [{}] with group id [{}]", topics, groupId);

        var jsonDeserializer = new JsonDeserializer<T>();
        jsonDeserializer.addTrustedPackages("*");
        var consumer = new KafkaConsumer<>(
                getConsumerProperties(groupId),
                new StringDeserializer(),
                jsonDeserializer
        );
        consumer.subscribe(Arrays.asList(topics));
        return consumer;
    }

    public static void unsubscribe(Consumer<?, ?> consumer) {
        log.info("Close consumer for topics {}", consumer.listTopics());
        consumer.unsubscribe();
        consumer.close();
    }

    public static <T> List<T> getRecordsFromKafka(Consumer<String, T> consumer, String topic) {
        var data = new ArrayList<T>();
        KafkaTestUtils.getRecords(consumer, Durations.TEN_SECONDS)
                .records(topic)
                .forEach(stringTConsumerRecord -> data.add(stringTConsumerRecord.value()));
        return data;
    }

    public static void resetTopicsOffsetToLastOffset(String groupId, String... topics){
        var consumer = buildConsumerAndSubscribeTo(groupId, topics);
        KafkaTestUtils.getRecords(consumer, Duration.ofMillis(100), 10);
        consumer.commitSync();
        unsubscribe(consumer);
    }

    private static Properties getConsumerProperties(String groupId) {
        var properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers());
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");

        return properties;
    }
}
