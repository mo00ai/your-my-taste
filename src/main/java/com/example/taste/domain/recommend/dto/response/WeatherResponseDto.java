package com.example.taste.domain.recommend.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class WeatherResponseDto {
	private String temp; // 기온
	private String rainAmount;  // 강수량
	private String rainStatus; //강수상태

	@Builder
	public WeatherResponseDto(String temp, String rainAmount, String rainStatus) {
		this.temp = temp;
		this.rainAmount = rainAmount;
		this.rainStatus = rainStatus;

	}
}
