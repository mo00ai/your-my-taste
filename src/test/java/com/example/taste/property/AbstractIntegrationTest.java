package com.example.taste.property;

import java.util.Optional;

import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public abstract class AbstractIntegrationTest {
	static {
		if (System.getProperty("spring.profiles.active") == null) {
			System.setProperty("spring.profiles.active",
				Optional.ofNullable(System.getenv("CI")).map(e -> "test-int-docker").orElse("test-int"));
		}
	}

}
