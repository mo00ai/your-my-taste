package com.example.taste.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

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
    public FirebaseMessaging firebaseMessaging() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            String firebaseConfigJson = String.format(
                "{\"type\":\"%s\",\"project_id\":\"%s\",\"private_key_id\":\"%s\",\"private_key\":\"%s\",\"client_email\":\"%s\",\"client_id\":\"%s\",\"auth_uri\":\"%s\",\"token_uri\":\"%s\",\"auth_provider_x509_cert_url\":\"%s\",\"client_x509_cert_url\":\"%s\",\"universe_domain\":\"%s\"}",
                type, projectId, privateKeyId, privateKey.replace("\\n", "\n"), clientEmail, clientId, authUri, tokenUri, authProviderX509CertUrl, clientX509CertUrl, universeDomain
            );

            InputStream serviceAccount = new ByteArrayInputStream(firebaseConfigJson.getBytes());

            FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

            FirebaseApp.initializeApp(options);
        }
        return FirebaseMessaging.getInstance();
    }
}
