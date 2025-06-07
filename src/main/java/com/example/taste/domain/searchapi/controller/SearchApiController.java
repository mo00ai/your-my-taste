package com.example.taste.domain.searchapi.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.taste.common.response.CommonResponse;
import com.example.taste.domain.searchapi.dto.NaverLocalSearchResponseDto;
import com.example.taste.domain.searchapi.service.SearchApiService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/search")
public class SearchApiController {

	private final SearchApiService naverSearchApiService;

	@GetMapping(produces = "application/json; charset=UTF-8")
	public CommonResponse<NaverLocalSearchResponseDto> searchKeywords(
		@RequestParam String query
	) {
		NaverLocalSearchResponseDto response = naverSearchApiService.searchLocal(query);
		return CommonResponse.ok(response);
	}

	@GetMapping("/test-json")
	public CommonResponse<NaverLocalSearchResponseDto> testJsonParsing() throws Exception {
		// 네이버 API에서 받은 실제 JSON 응답을 직접 넣어서 테스트
		String testJson = """
			{
			  "lastBuildDate": "Sun, 08 Jun 2025 03:31:29 +0900",
			  "total": 5,
			  "start": 1,
			  "display": 5,
			  "items": [
			    {
			      "title": "원조이동김미자할머니갈비 포천이동본점",
			      "link": "http://www.김미자할머니갈비.kr/",
			      "category": "한식>육류,고기요리",
			      "description": "",
			      "telephone": "",
			      "address": "경기도 포천시 이동면 장암리 216-3",
			      "roadAddress": "경기도 포천시 이동면 화동로 2087",
			      "mapx": "1273660437",
			      "mapy": "380340306"
			    }
			  ]
			}
			""";

		ObjectMapper mapper = new ObjectMapper();
		NaverLocalSearchResponseDto response = mapper.readValue(testJson, NaverLocalSearchResponseDto.class);

		System.out.println("테스트 결과 items size: " +
			(response.getItems() != null ? response.getItems().size() : "null"));

		return CommonResponse.ok(response);
	}

}
