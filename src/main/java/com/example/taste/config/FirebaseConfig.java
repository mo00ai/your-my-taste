package com.example.taste.config;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;

@Configuration
public class FirebaseConfig {

	@Value("${firebase.type}")
	private String type;
	@Value("${firebase.project_id}")
	private String projectId;
	@Value("${firebase.private_key_id}")
	private String privateKeyId;
	@Value("${firebase.private_key}")
	private String privateKey;
	@Value("${firebase.client_email}")
	private String clientEmail;
	@Value("${firebase.client_id}")
	private String clientId;
	@Value("${firebase.auth_uri}")
	private String authUri;
	@Value("${firebase.token_uri}")
	private String tokenUri;
	@Value("${firebase.auth_provider_x509_cert_url}")
	private String authProviderX509CertUrl;
	@Value("${firebase.client_x509_cert_url}")
	private String clientX509CertUrl;
	@Value("${firebase.universe_domain}")
	private String universeDomain;

	@Bean
	public FirebaseApp firebaseApp() throws IOException {
		Map<String, String> serviceAccountMap = new HashMap<>();
		serviceAccountMap.put("type", type);
		serviceAccountMap.put("project_id", projectId);
		serviceAccountMap.put("private_key_id", privateKeyId);
		serviceAccountMap.put("private_key", privateKey.replace("\\n", "\n")); // Handle newline characters
		serviceAccountMap.put("client_email", clientEmail);
		serviceAccountMap.put("client_id", clientId);
		serviceAccountMap.put("auth_uri", authUri);
		serviceAccountMap.put("token_uri", tokenUri);
		serviceAccountMap.put("auth_provider_x509_cert_url", authProviderX509CertUrl);
		serviceAccountMap.put("client_x509_cert_url", clientX509CertUrl);
		serviceAccountMap.put("universe_domain", universeDomain);

		ObjectMapper objectMapper = new ObjectMapper();
		String serviceAccountJson = objectMapper.writeValueAsString(serviceAccountMap);

		FirebaseOptions options = FirebaseOptions.builder()
			.setCredentials(GoogleCredentials.fromStream(new ByteArrayInputStream(serviceAccountJson.getBytes())))
			.build();

		return FirebaseApp.initializeApp(options);
	}

	@Bean
	public FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
		return FirebaseMessaging.getInstance(firebaseApp);
	}
}

