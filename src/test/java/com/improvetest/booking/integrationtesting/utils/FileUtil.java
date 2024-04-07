package com.improvetest.booking.integrationtesting.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileUtil {

    public static String loadFileAsString(String classpath){
        var classpathResource = new ClassPathResource(classpath);

        try {
            return new String(classpathResource.getInputStream().readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
