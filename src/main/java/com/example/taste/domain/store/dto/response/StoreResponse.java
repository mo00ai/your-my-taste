package com.example.taste.domain.store.dto.response;

import java.math.BigDecimal;
import java.util.List;

import com.example.taste.domain.store.entity.Store;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StoreResponse {
	private Long id;
	private String category;
	private String name;
	private String description;
	private String address;
	private String roadAddress;
	private BigDecimal mapx;
	private BigDecimal mapy;
	private List<String> reviewImages;

	public static StoreResponse create(Store store, List<String> imageUrls) {
		return StoreResponse.builder()
			.id(store.getId())
			.category(store.getCategory() != null ? store.getCategory().getName() : "카테고리 없음")
			.name(store.getName())
			.description(store.getDescription())
			.address(store.getAddress())
			.roadAddress(store.getRoadAddress())
			.mapx(store.getMapx())
			.mapy(store.getMapy())
			.reviewImages(imageUrls)
			.build();
	}
}
