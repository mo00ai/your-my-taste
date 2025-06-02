package com.example.taste.domain.store.dto.request;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AddBucketItemRequest {
	private Long storeId;
	private List<Long> bucketIds;
}
