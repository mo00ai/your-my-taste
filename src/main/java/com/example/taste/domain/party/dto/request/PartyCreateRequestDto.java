package com.example.taste.domain.party.dto.request;

import java.time.LocalDate;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import lombok.Getter;

import org.hibernate.validator.constraints.Range;

import com.example.taste.common.annotation.DateRange;
import com.example.taste.domain.match.dto.request.PartyMatchInfoSimpleCreateRequestDto;
import com.fasterxml.jackson.annotation.JsonFormat;

@Getter
public class PartyCreateRequestDto {
	@Positive(message = "유효하지 않은 가게 ID 정보입니다.")
	private Long storeId;

	@NotNull(message = "제목을 입력해주세요.")
	@Size(min = 1, max = 50, message = "제목은 50자 이하입니다.")
	private String title;

	@Size(min = 0, max = 500, message = "파티 설명은 500자 이내입니다.")
	private String description;

	@NotNull(message = "모임 시간은 필수입니다.")
	@DateRange(max = 30)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private LocalDate meetingDate;

	@NotNull(message = "최대 인원은 필수입니다.")
	@Range(min = 2, max = 16, message = "파티 인원은 2 ~ 16명 사이입니다.")
	private Integer maxMembers;

	@NotNull(message = "랜덤 매치 여부를 선택하세요.")
	private Boolean enableRandomMatching;

	@Valid
	private PartyMatchInfoSimpleCreateRequestDto partyMatchInfo;
}
