package com.example.taste.domain.board.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BoardUpdateRequestDto {
	private String title;
	private String contents;
	private String type;
	private String imageUrl;
}
