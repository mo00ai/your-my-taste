package com.example.taste.domain.store.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateBucketRequest{
	private String name; // Todo : validation
	private Boolean isOpened;
}
