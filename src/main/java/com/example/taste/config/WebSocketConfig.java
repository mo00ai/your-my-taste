package com.example.taste.config;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

import com.example.taste.common.websocket.CustomHandshakeHandler;
import com.example.taste.common.websocket.CustomHttpHandshakeInterceptor;
import com.example.taste.common.websocket.WebSocketAuthInterceptor;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
	private final WebSocketAuthInterceptor authInterceptor;
	private final CustomHttpHandshakeInterceptor customHttpHandshakeInterceptor;
	private final CustomHandshakeHandler customHandshakeHandler;

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/ws")
			.setAllowedOriginPatterns("*") // TODO 실서비스에서는 보안상 프론트엔드 url 적용 @김채진
			.addInterceptors(customHttpHandshakeInterceptor)
			.setHandshakeHandler(customHandshakeHandler)
			.withSockJS();
	}

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		registry.enableSimpleBroker("/sub"); // 해당 경로 구독중인 유저에게 메세지 전달
		registry.setApplicationDestinationPrefixes("/pub"); // 클라이언트가 서버로 메세지 전송할 때 prefix
		registry.setUserDestinationPrefix("/private"); // 사용자별 메시지 전송을 위한 prefix
	}

	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {
		registration.interceptors(authInterceptor);        // 메시지 전송 시 인증/권한 검증
		registration.taskExecutor()                        // 성능 최적화: 스레드 풀 설정
			.corePoolSize(10)
			.maxPoolSize(20)
			.queueCapacity(200);
	}

	@Override
	public void configureWebSocketTransport(WebSocketTransportRegistration registry) {
		// 성능 및 보안 최적화 설정
		registry.setMessageSizeLimit(128 * 1024)        // 128KB 메시지 크기 제한
			.setSendTimeLimit(10 * 1000)              // 10초 전송 타임아웃
			.setSendBufferSizeLimit(512 * 1024);      // 512KB 전송 버퍼 크기
	}
}
