package com.example.taste.property;

import java.util.Optional;

import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;

@SpringBootTest
public abstract class AbstractIntegrationTest {
	static {
		if (System.getProperty("spring.profiles.active") == null) {
			System.setProperty("spring.profiles.active",
				Optional.ofNullable(System.getenv("CI")).map(e -> "test-int-docker").orElse("test-int"));
		}
	}

	@MockitoBean
	protected FirebaseApp firebaseApp;

	@MockitoBean
	protected FirebaseMessaging firebaseMessaging;
}
