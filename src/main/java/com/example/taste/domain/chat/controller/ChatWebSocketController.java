package com.example.taste.domain.chat.controller;

import static com.example.taste.common.exception.ErrorCode.INVALID_SIGNATURE;

import java.security.Principal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import com.example.taste.common.exception.CustomException;
import com.example.taste.config.security.CustomUserDetails;
import com.example.taste.domain.chat.dto.ChatCreateRequestDto;
import com.example.taste.domain.chat.dto.ChatResponseDto;
import com.example.taste.domain.chat.service.ChatService;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {
	private final ChatService chatService;

	// 채팅방에 메세지 보내기
	@MessageMapping("/parties/{partyId}/chat/send")
	@SendTo("/sub/parties/{partyId}/chat")
	public ChatResponseDto sendMessage(
		@DestinationVariable Long partyId,
		@Payload ChatCreateRequestDto message, Principal principal) {
		if (!(principal instanceof Authentication authentication)) {
			throw new CustomException(INVALID_SIGNATURE);
		}

		Object inner = authentication.getPrincipal();
		if (!(inner instanceof CustomUserDetails customUserDetails)) {
			throw new CustomException(INVALID_SIGNATURE);
		}

		log.info("메세지 전송내용: {}, UserID: {}, PartyId: {}",
			message.getMessage(),
			customUserDetails.getUser().getId(),
			message.getPartyId());

		return chatService.saveMessage(customUserDetails.getUser(), message);
	}
}
