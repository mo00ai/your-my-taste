package com.example.taste.domain.chat.dto;

import java.time.LocalDateTime;

import lombok.Getter;

import com.example.taste.domain.chat.entity.Chat;
import com.fasterxml.jackson.annotation.JsonFormat;

@Getter
public class ChatResponseDto {
	private Long partyId;
	private Long senderId;
	private String senderNickname;
	private String senderImageUrl;
	private String message;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createdAt;

	public ChatResponseDto(Chat chat) {
		this.partyId = chat.getParty().getId();
		this.senderId = chat.getUser().getId();
		this.senderNickname = chat.getUser().getNickname();
		this.senderImageUrl = chat.getUser().getImage() != null ? chat.getUser().getImage().getUrl() : null;
		this.message = chat.getMessage();
		this.createdAt = chat.getCreatedAt();
	}
}
