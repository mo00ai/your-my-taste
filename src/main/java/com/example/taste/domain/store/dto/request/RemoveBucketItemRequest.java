package com.example.taste.domain.store.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RemoveBucketItemRequest {
	@NotEmpty
	private List<Long> bucketItemIds;
}
