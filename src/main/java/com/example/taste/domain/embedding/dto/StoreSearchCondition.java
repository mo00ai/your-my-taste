package com.example.taste.domain.embedding.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class StoreSearchCondition {
	// 필수: 임베딩 기반 의미적 검색
	@NotBlank(message = "검색어는 필수입니다")
	private String query;

	// 선택적 필터들
	private String category;
	// 위도, 경도는 String (기본값은 서울역 주소)
	private String latitude = "37.5548376";
	private String longitude = "126.9717326";
	// 미터 기준
	@Min(value = 100, message = "검색 반경은 최소 100m입니다")
	@Max(value = 10000, message = "검색 반경은 최대 10km입니다")
	private Integer radius = 3000; // 기본값 3km

	/**
	 * 0.8~0.9: 매우 유사 (정확한 매칭)
	 * 0.7~0.8: 유사 (일반적 검색)
	 * 0.6~0.7: 관련 있음 (확장 검색)
	 */
	@DecimalMin(value = "0.0", message = "유사도 임계값은 0.0 이상이어야 합니다")
	@DecimalMax(value = "1.0", message = "유사도 임계값은 1.0 이하여야 합니다")
	private Double similarityThreshold = 0.7; // 기본값 0.7

	public boolean hasGeo() {
		return latitude != null && !latitude.isBlank()
			&& longitude != null && !longitude.isBlank();
	}
}
