package com.example.taste.domain.recommend.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.taste.domain.recommend.dto.ai.ChatMessage;
import com.example.taste.domain.recommend.dto.ai.ChatRequestDto;
import com.example.taste.domain.recommend.dto.ai.ChatResponseDto;
import com.example.taste.domain.user.entity.UserFavor;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AiResponseService {

	@Value("${openai.model}")
	private String model;

	private final WebClient aiWebClient;

	public Mono<String> recommendFood(String userMessage, List<UserFavor> favors, String temp, String rainAmount,
		String rainStatus) {

		String favorList = favors.stream()
			.map(userFavor -> userFavor.getFavor().getName())
			.collect(Collectors.joining(", "));

		String prompt = String.format(
			"취향: '%s', 기온: '%s', 강수량: '%s', 강수 상태: '%s'.\n" +
				"이 정보와 사용자 메시지를 참고해 메뉴 1가지를 추천해 주세요.\n" +
				"문장은 2줄 정도로 간결하게 하지만 친근하게 추천하는 이유와 함께 답해주세요.",
			favorList, temp, rainAmount, rainStatus);

		ChatRequestDto request = new ChatRequestDto(
			model,
			List.of(
				new ChatMessage("system", prompt),
				new ChatMessage("user", userMessage)
			)
		);

		return aiWebClient.post()
			.uri("/chat/completions")
			.bodyValue(request)
			.retrieve()
			.bodyToMono(ChatResponseDto.class)
			.map(response -> response.getChoices().get(0).getMessage().getContent());

	}

}
