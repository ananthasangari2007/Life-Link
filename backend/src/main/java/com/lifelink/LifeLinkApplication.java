package com.lifelink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

/**
 * LifeLink — Smart Blood Bank Network & Hospital Coordination System.
 * Main Application Bootstrap.
 */
@SpringBootApplication
@EnableScheduling
public class LifeLinkApplication {

    public static void main(String[] args) {
        // Enforce consistent UTC timezone for server-side persistence
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SpringApplication.run(LifeLinkApplication.class, args);
    }
}
