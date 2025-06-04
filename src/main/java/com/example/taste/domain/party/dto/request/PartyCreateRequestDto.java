package com.example.taste.domain.party.dto.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import lombok.Getter;

import org.hibernate.validator.constraints.Range;

@Getter
public class PartyCreateRequestDto {
	@Positive(message = "유효하지 않은 가게 ID 정보입니다.")
	private Long storeId;

	@NotNull(message = "제목을 입력해주세요.")
	@Size(min = 1, max = 50, message = "제목은 50자 이하입니다.")
	private String title;

	@Size(min = 0, max = 500, message = "파티 설명은 500자 이내입니다.")
	private String description;

	@FutureOrPresent(message = "현재 이후의 시간이어야 합니다.")
	private LocalDateTime meetingTime;

	@NotNull(message = "최대 인원은 필수 입력값입니다.")
	@Range(min = 2, max = 16, message = "파티 인원은 2 ~ 16명 사이입니다.")
	private Integer maxMembers;

	@NotNull(message = "랜덤 매치 여부를 선택하세요.")
	private Boolean isRandomMatch;
}
