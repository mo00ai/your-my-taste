package com.example.taste.domain.favor.dto.request;

import java.util.List;

import lombok.Getter;

@Getter
public class FavorAdminListCreateRequestDto {
	private List<FavorAdminRequestDto> favorList;        // TODO: 리스트 항목에 대하여 유효성 검증
}
