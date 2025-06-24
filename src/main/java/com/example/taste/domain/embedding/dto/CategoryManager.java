package com.example.taste.domain.embedding.dto;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class CategoryManager {
	// 검색 필터( 카테고리, 지역명)
	private static Set<String> FOOD_CATEGORIES = ConcurrentHashMap.newKeySet();
	private static Set<String> REGION_KEYWORDS = ConcurrentHashMap.newKeySet();

	// 검색 빈도 추적
	private final Map<String, Integer> categorySearchCount = new ConcurrentHashMap<>();
	private final Map<String, Integer> unknownTermCount = new ConcurrentHashMap<>();

	@PostConstruct
	public void initializeCategories() {
		// 기본 카테고리들 초기화
		FOOD_CATEGORIES.addAll(Set.of(
			"한식", "중식", "일식", "양식", "이탈리안", "프렌치",
			"치킨", "피자", "카페", "디저트", "바"
		));

		REGION_KEYWORDS.addAll(Set.of(
			"강남", "홍대", "명동", "이태원", "신촌", "건대",
			"서울", "부산", "대구", "인천", "광주"
		));
	}

	public boolean isValidCategory(String category) {
		// 검색 빈도 카운트
		categorySearchCount.merge(category, 1, Integer::sum);
		return FOOD_CATEGORIES.contains(category);
	}
}
