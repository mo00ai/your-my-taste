package com.example.taste.domain.user.dto.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import lombok.Getter;

@Getter
public class UserFavorUpdateListRequestDto {
	@Valid
	@Size(min = 1, max = 5, message = "취향 입맛은 1개 이상 5개 이하로 입력해야 합니다.")
	private List<UserFavorUpdateRequestDto> userFavorList;
}
