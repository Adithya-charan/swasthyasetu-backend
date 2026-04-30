package com.swasthyasetu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class SwasthyaSetuApplication {
    public static void main(String[] args) {
        SpringApplication.run(SwasthyaSetuApplication.class, args);
    }
}
