package com.example.taste.domain.pk.dto.response;

import java.io.Serial;
import java.io.Serializable;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PkCriteriaResponseDto implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	private Long id;
	private String type;
	private Integer point;
	private boolean active;

	@Builder
	public PkCriteriaResponseDto(Long id, String type, Integer point, boolean active) {
		this.id = id;
		this.type = type;
		this.point = point;
		this.active = active;
	}
}
