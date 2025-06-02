package com.example.taste.domain.store.dto.request;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RemoveBucketItemRequest {
	private List<Long> bucketItemIds;
}
