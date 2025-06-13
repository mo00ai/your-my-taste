package com.example.taste.domain.match.dto.response;

import lombok.Builder;
import lombok.Getter;

import com.example.taste.domain.match.entity.UserMatchInfoStore;

@Getter
public class UserMatchInfoStoreResponseDto {
	private Long userMatchInfoStoreId;
	private Long storeId;
	private String name;

	@Builder
	public UserMatchInfoStoreResponseDto(UserMatchInfoStore matchInfoStore) {
		this.userMatchInfoStoreId = matchInfoStore.getId();
		this.storeId = matchInfoStore.getStore().getId();
		this.name = matchInfoStore.getStore().getName();
	}
}
