package com.example.taste.domain.match.dto.request;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Getter;

import com.example.taste.common.annotation.DateRange;
import com.example.taste.domain.match.vo.AgeRange;
import com.fasterxml.jackson.annotation.JsonFormat;

@Getter
public class UserMatchInfoCreateRequestDto {
	@NotNull(message = "제목을 입력해주세요.")
	@Size(min = 1, max = 50, message = "제목은 50자 이하입니다.")
	private String title;

	private AgeRange ageRange;

	@DateRange(max = 30)
	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate meetingDate;

	private String region;

	@Size(min = 0, max = 5, message = "선호 카테고리는 5개 이하로 입력해야 합니다.")
	private List<String> categoryList;

	@Size(min = 0, max = 5, message = "선호 입맛은 5개 이하로 입력해야 합니다.")
	private List<String> favorList;

	@Size(min = 0, max = 3, message = "선호 맛집은 3개 이하로 입력해야 합니다.")
	private List<Long> storeList;
}
