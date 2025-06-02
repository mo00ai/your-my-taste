package com.example.taste.domain.store.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AddBucketItemRequest {
	@NotNull
	private Long storeId;
	@NotEmpty
	private List<Long> bucketIds;
}
