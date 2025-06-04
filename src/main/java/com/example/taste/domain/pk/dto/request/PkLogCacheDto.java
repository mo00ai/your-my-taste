package com.example.taste.domain.pk.dto.request;

import java.time.LocalDateTime;

import com.example.taste.domain.pk.entity.PkLog;
import com.example.taste.domain.pk.enums.PkType;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PkLogCacheDto {

	private Long userId;
	private PkType pkType;
	private int point;
	private LocalDateTime createdAt;

	public static PkLogCacheDto from(PkLog pkLog) {
		return new PkLogCacheDto(
			pkLog.getUser().getId(),
			pkLog.getPkType(),
			pkLog.getPoint(),
			pkLog.getCreatedAt()
		);
	}

	@Builder
	public PkLogCacheDto(Long userId, PkType pkType, int point, LocalDateTime createdAt) {
		this.userId = userId;
		this.pkType = pkType;
		this.point = point;
		this.createdAt = createdAt;
	}
}
