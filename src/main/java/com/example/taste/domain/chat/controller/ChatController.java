package com.example.taste.domain.chat.controller;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.example.taste.common.response.CommonResponse;
import com.example.taste.domain.chat.dto.ChatResponseDto;
import com.example.taste.domain.chat.service.ChatService;
import com.example.taste.domain.user.entity.CustomUserDetails;

@RestController
@RequiredArgsConstructor
public class ChatController {
	private final ChatService chatService;

	// 채팅방 채팅 목록 불러오기
	@GetMapping("/parties/{partyId}/chats")
	public CommonResponse<List<ChatResponseDto>> getChats(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@PathVariable Long partyId) {
		return CommonResponse.ok(chatService.getChats(userDetails.getId(), partyId));
	}
}
