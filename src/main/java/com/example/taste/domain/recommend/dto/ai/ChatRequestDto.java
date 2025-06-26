package com.example.taste.domain.recommend.dto.ai;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChatRequestDto {

	private String model;
	private List<ChatMessage> messages;
	private int n;
	private int max_tokens;

	@Builder
	public ChatRequestDto(String model, List<ChatMessage> messages) {

		this.model = model;
		this.messages = messages;
		this.n = 1;
		this.max_tokens = 200;
	}

}
