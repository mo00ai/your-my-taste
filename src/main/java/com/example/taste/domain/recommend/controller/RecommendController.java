package com.example.taste.domain.recommend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.taste.domain.recommend.service.RecommendService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/recommends")
public class RecommendController {

	private final RecommendService recommendService;

	// @GetMapping
	// public Mono<String> recommend(@RequestParam String address, @RequestParam String userId) {
	// 	return recommendService.recommend(address, userId);
	// }

}
