package com.example.taste.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import nl.martijndwars.webpush.PushService;

@Configuration
public class WebPushConfig {

	@Bean
	public PushService pushService() {
		return new PushService();
	}
}
