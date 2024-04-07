package com.improvetest.booking.integrationtesting.initializer;

import org.jetbrains.annotations.NotNull;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.MongoDBContainer;

import java.util.Map;


public class MongoInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    public static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.0")
            .withTmpFs(Map.of("/data/db", "rw"));
    static {
        mongoDBContainer.start();
    }

    @Override
    public void initialize(@NotNull ConfigurableApplicationContext applicationContext) {
        TestPropertyValues
                .of(Map.of("spring.data.mongodb.uri", mongoDBContainer.getReplicaSetUrl()))
                .applyTo(applicationContext);
    }
}
