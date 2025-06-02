package com.example.taste.domain.favor.dto.request;

import java.util.List;

import lombok.Getter;

@Getter
public class FavorListCreateRequestDto {
	private List<FavorRequestDto> favorList;        // TODO: 리스트 항목에 대하여 유효성 검증
}
