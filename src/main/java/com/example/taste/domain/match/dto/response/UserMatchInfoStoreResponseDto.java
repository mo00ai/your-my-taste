package com.example.taste.domain.match.dto.response;

import lombok.Builder;
import lombok.Getter;

import com.example.taste.domain.match.entity.UserMatchInfoStore;

@Getter
public class UserMatchInfoStoreResponseDto {
	private Long condStoreId;
	private Long storeId;
	private String name;

	@Builder
	public UserMatchInfoStoreResponseDto(UserMatchInfoStore matchInfoStore) {
		this.condStoreId = matchInfoStore.getId();
		this.storeId = matchInfoStore.getStore().getId();
		this.name = matchInfoStore.getStore().getName();
	}
}
