package com.example.taste.domain.board.dto.search;

import com.example.taste.common.annotation.ValidEnum;
import com.example.taste.domain.board.entity.AccessPolicy;
import com.example.taste.domain.board.entity.BoardType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.Valid;
import lombok.Data;
import lombok.Getter;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BoardSearchCondition {
	// 키워드로 제목+내용을 동시에 검색하는 경우
	private String keyword;
	// 필드별 검색
	private String title;
	private String contents;
	private String storeName;
	private String authorName;

	// 필터링
	@ValidEnum(target = BoardType.class)
	private String type;    // "N", "O"
	@ValidEnum(target = AccessPolicy.class)
	private String accessPolicy;    // "OPEN", "CLOSED",  "FCFS", "TIMEATTACK"
	// 날짜 범위
	@Valid
	private CreatedDateRange dateRange = new CreatedDateRange();

	private String foodCategory;
	private String location;

	// 검색 전략 선택
	@ValidEnum(target = SearchStrategy.class)
	private String searchStrategy;

	@Getter
	public enum SearchStrategy {
		EXACT("정확한 키워드 매칭"),
		FUZZY("유사도 검색"),
		HYBRID("하이브리드 검색"),
		CATEGORY("카테고리 중심"),
		LOCATION("지역 중심"),
		;

		private final String displayName;

		SearchStrategy(String displayName) {
			this.displayName = displayName;
		}
	}
}
