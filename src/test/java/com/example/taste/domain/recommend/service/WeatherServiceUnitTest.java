package com.example.taste.domain.recommend.service;

import static org.assertj.core.api.AssertionsForClassTypes.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class WeatherServiceUnitTest {

	@InjectMocks
	private WeatherService weatherService;

	@Test
	void addTempCelsius() {
		// given
		String temp = "25";

		// when
		String result = weatherService.addTempCelsius(temp);

		// then
		assertThat(result).isEqualTo("25℃");
	}

	@Test
	void convertRainAmount() {
		// given
		String rainAmount = "2";

		// when
		String result = weatherService.convertRainAmount(rainAmount);

		// then
		assertThat(result).isEqualTo("2mm");
	}

	@Test
	void convertRainStatus() {
		// given
		String pty = "1";

		// when
		String result = weatherService.convertRainStatus(pty);

		// then
		assertThat(result).isEqualTo("비");
	}
}
