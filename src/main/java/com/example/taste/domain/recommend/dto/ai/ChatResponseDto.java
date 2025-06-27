package com.example.taste.domain.recommend.dto.ai;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChatResponseDto {

	private List<Choice> choices = new ArrayList<>();

	@Getter
	@NoArgsConstructor
	public static class Choice {
		private int index;
		private ChatMessage message;
	}

}
