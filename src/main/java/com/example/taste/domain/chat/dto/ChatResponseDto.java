package com.example.taste.domain.chat.dto;

import static com.example.taste.common.constant.CommonConst.DELETED_USER_NICKNAME;

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

	// 탈퇴한 사용자라면 닉네임 마스킹
	public ChatResponseDto(Chat chat) {
		this.partyId = chat.getParty().getId();
		this.senderId = chat.getUser().getId();
		this.senderNickname = chat.getUser().getDeletedAt() == null ?
			chat.getUser().getNickname() : DELETED_USER_NICKNAME;
		if (chat.getUser().getDeletedAt() == null) {
			if (chat.getUser().getImage() != null) {
				this.senderImageUrl = chat.getUser().getImage().getUrl();
			} else {
				this.senderImageUrl = null;
			}
		} else {
			this.senderImageUrl = null;
		}
		this.message = chat.getMessage();
		this.createdAt = chat.getCreatedAt();
	}
}
