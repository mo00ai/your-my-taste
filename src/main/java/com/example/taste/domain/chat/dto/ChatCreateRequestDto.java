package com.example.taste.domain.chat.dto;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class ChatCreateRequestDto {
	private Long partyId;
	private Long senderId;
	private String message;

	@JsonCreator
	public ChatCreateRequestDto(
		@JsonProperty("partyId") Long partyId, @JsonProperty("senderId") Long senderId,
		@JsonProperty("message") String message) {
		this.partyId = partyId;
		this.senderId = senderId;
		this.message = message;
	}
}
