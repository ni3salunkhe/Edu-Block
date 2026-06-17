package com.edu.edublock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;

import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan(basePackages = "com.edu")
@EnableJpaRepositories(basePackages = "com.edu.repositories")
@EntityScan(basePackages = "com.edu.entity")
@EnableAsync
@EnableScheduling
public class EdublockApplication {

    public static void main(String[] args) {
        SpringApplication.run(EdublockApplication.class, args);
        System.out.println("IPv4 preferred = " + System.getProperty("java.net.preferIPv4Stack"));
    }
}
