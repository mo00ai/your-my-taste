package com.example.taste.domain.board.dto.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class OpenRunBoardRequestDto extends BoardRequestDto {
	@NotNull(message = "openLimit은 필수 입력값입니다.")
	private int openLimit;

	@FutureOrPresent(message = "openTime은 현재 시간 이후여야 합니다.")
	@NotNull(message = "openTime은 필수 입력값입니다.")
	private LocalDateTime openTime;
}
