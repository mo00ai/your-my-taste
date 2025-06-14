package com.example.taste.domain.chat.dto;

import lombok.Getter;

@Getter
public class ChatMessageCreateRequestDto {
	private Long partyId;
	private Long senderId;
	private String message;
}
