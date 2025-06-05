package com.example.taste.domain.board.dto.request;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class OpenRunBoardRequestDto extends BoardRequestDto {
	private int openLimit;
	private LocalDateTime openTime;
}
