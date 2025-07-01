package com.example.taste.domain.recommend.dto.ai;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChatMessage {

	private String role;
	private String content;

	@Builder
	public ChatMessage(String role, String content) {
		this.role = role;
		this.content = content;
	}
}
