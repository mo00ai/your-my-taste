package com.example.taste.domain.match.dto.response;

import lombok.Builder;
import lombok.Getter;

import com.example.taste.domain.match.entity.UserMatchCondStore;

@Getter
public class UserMatchCondStoreResponseDto {
	private Long condStoreId;
	private Long storeId;
	private String name;

	@Builder
	public UserMatchCondStoreResponseDto(UserMatchCondStore matchCondStore) {
		this.condStoreId = matchCondStore.getId();
		this.storeId = matchCondStore.getStore().getId();
		this.name = matchCondStore.getStore().getName();
	}
}
