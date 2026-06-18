package com.neoapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NeoAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(NeoAppApplication.class, args);
    }
}
