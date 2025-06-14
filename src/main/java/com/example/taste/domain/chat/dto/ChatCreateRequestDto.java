package com.example.taste.domain.chat.dto;

import lombok.Getter;

@Getter
public class ChatCreateRequestDto {
	private Long partyId;
	private Long senderId;
	private String message;
}
