package com.swasthyasetu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class SwasthyaSetuApplication implements org.springframework.boot.CommandLineRunner {
    public static void main(String[] args) {
        SpringApplication.run(SwasthyaSetuApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("--- DB DIAGNOSTIC ---");
        System.out.println("DB_HOST: " + System.getenv("DB_HOST"));
        System.out.println("DB_USER: " + System.getenv("DB_USER"));
        System.out.println("DB_PORT: " + System.getenv("DB_PORT"));
        System.out.println("DB_PASSWORD SET: " + (System.getenv("DB_PASSWORD") != null && !System.getenv("DB_PASSWORD").isEmpty()));
        System.out.println("---------------------");
    }
}
