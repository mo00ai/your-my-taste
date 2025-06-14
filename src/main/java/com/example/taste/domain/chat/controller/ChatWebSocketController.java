package com.example.taste.domain.chat.controller;

import java.security.Principal;

import lombok.RequiredArgsConstructor;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import com.example.taste.config.security.CustomUserDetails;
import com.example.taste.domain.chat.dto.ChatCreateRequestDto;
import com.example.taste.domain.chat.dto.ChatResponseDto;
import com.example.taste.domain.chat.service.ChatService;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {
	private final ChatService chatService;

	// 채팅방에 메세지 보내기
	@MessageMapping("/pub/parties/{partyId}/chat/send")
	@SendTo("/sub/parties/{partyId}/chat")
	public ChatResponseDto sendMessage(@DestinationVariable("partyId") Long partyId,
		@Payload ChatCreateRequestDto message, Principal principal) {
		CustomUserDetails customUserDetails = (CustomUserDetails)principal;
		return chatService.saveMessage(customUserDetails.getUser(), message);
	}
}
