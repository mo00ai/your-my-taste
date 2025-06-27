package com.example.taste.domain.embedding.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.taste.common.response.CommonResponse;
import com.example.taste.domain.embedding.dto.EmbeddingRequest;
import com.example.taste.domain.embedding.service.EmbeddingService;
import com.example.taste.domain.store.service.StoreEmbeddingUpdater;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RequestMapping("/api/test/em")
@RequiredArgsConstructor
@RestController
public class EmbeddingController {

	private final EmbeddingService embeddingService;
	private final StoreEmbeddingUpdater updater;

	@PostMapping
	public CommonResponse<?> createEmbedding(
		@Valid @RequestBody EmbeddingRequest request
	) {
		embeddingService.createEmbeddingTest(request.getText());
		return CommonResponse.ok("임베딩이 성공적으로 생성되었습니다");

	}

	@GetMapping
	public CommonResponse<?> korean() {
		String test = "안녕하세요 저는 한국인 입니다!";
		embeddingService.stemming(test);
		return CommonResponse.ok("테스트");

	}

	@PostMapping("/run")
	public CommonResponse<Void> runEmbedding() {
		updater.fillEmptyEmbeddings();
		return CommonResponse.ok();
	}

	// @GetMapping("/line")
	// public CommonResponse<?> test() {
	// 	List<Station> stations = stationService.loadLineStations();
	// 	return CommonResponse.ok(stations);
	// }
}
