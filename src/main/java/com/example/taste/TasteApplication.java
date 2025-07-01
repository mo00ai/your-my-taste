package com.example.taste;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@ConfigurationPropertiesScan
@EnableScheduling
@EnableCaching
@EnableJpaAuditing
@SpringBootApplication
public class TasteApplication {
	public static void main(String[] args) {
		SpringApplication.run(TasteApplication.class, args);
	}

}
