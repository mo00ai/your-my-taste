package com.example.taste.domain.user.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserFavorUpdateRequestDto {
	private Long userFavorId;
	private String name;
}
