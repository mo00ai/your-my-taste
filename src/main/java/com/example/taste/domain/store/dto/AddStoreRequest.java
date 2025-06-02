package com.example.taste.domain.store.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AddStoreRequest {
	private Long storeId;
	private List<Long> bucketIds;
}
