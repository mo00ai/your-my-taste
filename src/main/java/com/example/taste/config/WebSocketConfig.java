package com.example.taste.config;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

import com.example.taste.common.interceptor.CustomHttpHandshakeInterceptor;
import com.example.taste.common.interceptor.StompCommandInterceptor;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
	private final StompCommandInterceptor stompCommandInterceptor;
	private final CustomHttpHandshakeInterceptor customHttpHandshakeInterceptor;

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		// 브라우저 환경용 - http://.../ws로 요청
		registry.addEndpoint("/ws")
			.setAllowedOriginPatterns("*") // TODO 도메인 주소 적용
			.addInterceptors(customHttpHandshakeInterceptor)
			.withSockJS()
			.setHeartbeatTime(10000);            // 10초마다 세션 서버 -> 클라이언트 살아있는지 확인, 응답 없으면 kill

		// 테스트 환경용 - ws://.../ws로 요청
		registry.addEndpoint("/ws")
			.setAllowedOriginPatterns("*")
			.addInterceptors(customHttpHandshakeInterceptor);
	}

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		registry.enableSimpleBroker("/sub"); // 해당 경로 구독중인 유저에게 메세지 전달
		registry.setApplicationDestinationPrefixes("/pub"); // 클라이언트가 서버로 메세지 전송할 때 prefix
		registry.setUserDestinationPrefix("/private"); // 사용자별 메시지 전송을 위한 prefix
	}

	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {
		registration.interceptors(stompCommandInterceptor);        // 메시지 전송 시 타입 따라 검증
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
