package com.example.taste.domain.match.dto.request;

import java.util.List;

import lombok.Getter;

@Getter    // TODO: VALID 추가
public class UserMatchCondCreateRequestDto {
	private String title;
	private Integer ageMinRange;
	private Integer ageMaxRange;
	private String gender;
	private String region;
	private List<String> categories;
	private List<Long> stores;
}
