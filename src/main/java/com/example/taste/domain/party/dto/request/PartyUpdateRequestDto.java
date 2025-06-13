package com.example.taste.domain.party.dto.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import lombok.Getter;

import org.hibernate.validator.constraints.Range;

import com.example.taste.common.annotation.DateRange;
import com.fasterxml.jackson.annotation.JsonFormat;

@Getter
public class PartyUpdateRequestDto {
	@Positive(message = "유효하지 않은 가게 ID 정보입니다.")
	private Long storeId;

	@Size(min = 1, max = 50, message = "제목은 50자 이하입니다.")
	private String title;

	@Size(min = 0, max = 500, message = "파티 설명은 500자 이내입니다.")
	private String description;

	@DateRange(max = 30)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
	private LocalDateTime meetingDate;

	@Range(min = 2, max = 16, message = "파티 인원은 2 ~ 16명 사이입니다.")
	private Integer maxMembers;
	private Boolean enableRandomMatching;
}
