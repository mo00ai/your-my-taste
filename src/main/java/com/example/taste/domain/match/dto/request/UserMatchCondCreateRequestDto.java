package com.example.taste.domain.match.dto.request;

import java.util.List;

import lombok.Getter;

import com.example.taste.domain.match.vo.AgeRange;

@Getter    // TODO: VALID 추가
public class UserMatchCondCreateRequestDto {
	private String title;
	private AgeRange ageRange;
	// private String gender;
	private String meetingDate;
	private String region;
	private List<String> categories;
	private List<Long> stores;
}
