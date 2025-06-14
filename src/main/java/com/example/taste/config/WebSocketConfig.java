package com.example.taste.common.response;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/ws")
			.setAllowedOriginPatterns("*") // TODO 실서비스에서는 보안상 프론트엔드 url 적용 @김채진
			.withSockJS();
	}

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		registry.enableSimpleBroker("/topic"); // 해당 경로 구독중인 유저에게 메세지 전달
		registry.setApplicationDestinationPrefixes("/app"); // 클라이언트가 서버로 메세지 전송할 때 prefix
	}
}
