package com.example.taste.common.handler;

import lombok.extern.slf4j.Slf4j;

import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;

@Slf4j
@ControllerAdvice
public class WebSocketExceptionHandler {
	@MessageExceptionHandler
	public void handleException(Exception e) {
		log.error("STOMP 예외 발생: {}", e.getMessage());
		e.printStackTrace();
	}
}
