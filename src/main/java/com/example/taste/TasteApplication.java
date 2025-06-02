package com.example.taste;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import com.example.taste.config.NaverConfig;

@EnableConfigurationProperties(NaverConfig.class)
@EnableJpaAuditing
@SpringBootApplication
public class TasteApplication {

	public static void main(String[] args) {
		SpringApplication.run(TasteApplication.class, args);
	}

}
