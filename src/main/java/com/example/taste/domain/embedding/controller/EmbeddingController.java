package com.example.taste.domain.embedding.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.taste.common.response.CommonResponse;
import com.example.taste.domain.embedding.dto.EmbeddingRequest;
import com.example.taste.domain.embedding.service.EmbeddingService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RequestMapping("/api/test" )
@RequiredArgsConstructor
@RestController
public class EmbeddingController {

	private final EmbeddingService embeddingService;

	@PostMapping
	public CommonResponse<?> createEmbedding(
		@Valid @RequestBody EmbeddingRequest request
	) {
		embeddingService.createEmbedding(request.getText());
		return CommonResponse.ok("임베딩이 성공적으로 생성되었습니다" );

	}
}
