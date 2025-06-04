package com.example.taste.domain.pk.dto.response;

import java.time.LocalDateTime;

import com.example.taste.domain.pk.enums.PkType;

import lombok.Builder;
import lombok.Getter;

@Getter
public class PkLogResponseDto {

	private Long logId;
	private Long userId;
	private PkType pkType;
	private int point;
	private LocalDateTime createdAt;

	@Builder
	public PkLogResponseDto(Long logId, Long userId, PkType pkType, int point, LocalDateTime createdAt) {
		this.logId = logId;
		this.userId = userId;
		this.pkType = pkType;
		this.point = point;
		this.createdAt = createdAt;
	}
}
