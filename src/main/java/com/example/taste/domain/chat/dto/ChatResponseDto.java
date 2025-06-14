package com.example.taste.domain.chat.dto;

import lombok.Getter;

import com.example.taste.domain.chat.entity.Chat;

@Getter
public class ChatResponseDto {
	private Long partyId;
	private Long senderId;
	private String senderNickname;
	private String senderImageUrl;
	private String message;

	public ChatResponseDto(Chat chat) {
		this.partyId = chat.getParty().getId();
		this.senderId = chat.getUser().getId();
		this.senderNickname = chat.getUser().getNickname();
		this.senderImageUrl = chat.getUser().getImage() != null ? chat.getUser().getImage().getUrl() : null;
		this.message = chat.getMessage();
	}
}
