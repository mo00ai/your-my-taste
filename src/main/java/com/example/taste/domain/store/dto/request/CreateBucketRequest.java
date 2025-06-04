package com.example.taste.domain.store.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateBucketRequest {
	@Size(max = 30, message = "버킷 이름은 30자 이하로 입력해주세요")
	private String name;
	private Boolean isOpened;
}
