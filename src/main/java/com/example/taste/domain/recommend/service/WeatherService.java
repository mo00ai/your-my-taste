package com.example.taste.domain.recommend.service;

import static com.example.taste.domain.recommend.exception.RecommendErrorCode.*;
import static com.example.taste.domain.recommend.util.GridUtil.*;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.taste.common.exception.CustomException;
import com.example.taste.domain.recommend.dto.response.FcstResponseDto;
import com.example.taste.domain.recommend.dto.response.WeatherResponseDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherService {

	@Value("${weather.api.key}")
	private String weatherKey;

	private final WebClient weatherWebClient;

	public Mono<WeatherResponseDto> loadWeather(double lat, double lon) {

		// 좌표 변환
		LatXLngY grid = convert(TO_GRID, lat, lon);

		String baseDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		String baseTime = LocalDateTime.now().minusMinutes(30).format(DateTimeFormatter.ofPattern("HHmm"));

		//url 인코딩 문제 때문에 부득이하게 String으로...
		String url = "https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtNcst"
			+ "?serviceKey=" + weatherKey
			+ "&pageNo=1"
			+ "&numOfRows=100"
			+ "&dataType=JSON"
			+ "&base_date=" + baseDate
			+ "&base_time=" + baseTime
			+ "&nx=" + (int)grid.x
			+ "&ny=" + (int)grid.y;

		// 요청 및 파싱
		Mono<WeatherResponseDto> weatherResult = weatherWebClient.get()
			.uri(URI.create(url))
			.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
			.retrieve()
			.bodyToMono(FcstResponseDto.class)
			.map(dto -> Optional.ofNullable(dto.getResponse())
				.map(FcstResponseDto.Response::getBody)
				.map(FcstResponseDto.Body::getItems)
				.map(FcstResponseDto.Items::getItem)
				.orElseThrow(() -> new CustomException(WEATHER_LOAD_FAILED))
			)
			.map(items -> {
				String t1h = items.stream()
					.filter(i -> "T1H".equals(i.getCategory()))
					.map(FcstResponseDto.Item::getObsrValue)
					.findFirst()
					.orElse("알 수 없음");

				String rn1 = items.stream()
					.filter(i -> "RN1".equals(i.getCategory()))
					.map(FcstResponseDto.Item::getObsrValue)
					.findFirst()
					.orElse("알 수 없음");

				String pty = items.stream()
					.filter(i -> "PTY".equals(i.getCategory()))
					.map(FcstResponseDto.Item::getObsrValue)
					.findFirst()
					.orElse("알 수 없음");

				return WeatherResponseDto.builder()
					.temp(addTempCelsius(t1h))
					.rainAmount(convertRainAmount(rn1))
					.rainStatus(convertRainStatus(pty))
					.build();
			});

		return weatherResult;
	}

	//데이터 변환 메서드들
	protected String addTempCelsius(String temp) {
		if ("알 수 없음".equals(temp)) {
			return temp;
		}
		return temp + "℃";
	}

	protected String convertRainAmount(String rainAmount) {
		if (rainAmount == null || "0".equals(rainAmount) || "-".equals(rainAmount)) {
			return "강수 없음";
		}
		return rainAmount + "mm";
	}

	protected String convertRainStatus(String pty) {
		String result;

		switch (pty) {
			case "0" -> result = "강수 없음";
			case "1" -> result = "비";
			case "2" -> result = "비/눈";
			case "3" -> result = "눈";
			case "5" -> result = "빗방울";
			case "6" -> result = "빗방울눈날림";
			case "7" -> result = "눈날림";

			default -> result = "알 수 없음";
		}

		return result;
	}

}
