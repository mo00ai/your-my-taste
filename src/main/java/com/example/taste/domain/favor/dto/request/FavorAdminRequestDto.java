package com.example.taste.domain.favor.dto.request;

import jakarta.validation.constraints.Size;

import lombok.Getter;

@Getter
public class FavorAdminRequestDto {
	@Size(min = 1, max = 10, message = "입맛 취향명은 1자 이상 10자 이하로 입력해주세요.")
	private String favorName;
}
