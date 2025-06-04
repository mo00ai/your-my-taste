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
	@NotEmpty(message = "필수 요청값이 생략되었습니다.")
	private List<Long> bucketItemIds;
}
