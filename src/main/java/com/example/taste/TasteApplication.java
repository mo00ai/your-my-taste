package com.example.taste;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class TasteApplication {

	public static void main(String[] args) {
		SpringApplication.run(TasteApplication.class, args);
	}

}
