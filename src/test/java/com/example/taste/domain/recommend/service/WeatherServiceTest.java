package com.example.taste.domain.recommend.service;

import static org.assertj.core.api.AssertionsForClassTypes.*;

import java.io.IOException;
import java.lang.reflect.Field;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.taste.domain.recommend.dto.response.WeatherResponseDto;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class WeatherServiceTest {

	private MockWebServer mockWebServer;
	private WeatherService weatherService;

	@BeforeEach
	void setup() throws IOException, NoSuchFieldException, IllegalAccessException {
		mockWebServer = new MockWebServer();
		mockWebServer.start();

		WebClient webClient = WebClient.builder()
			.baseUrl(mockWebServer.url("/").toString())
			.build();

		weatherService = new WeatherService(webClient);

		// 리플렉션으로 weatherKey 주입
		Field field = WeatherService.class.getDeclaredField("weatherKey");
		field.setAccessible(true);
		field.set(weatherService, "dummyWeatherKey");
	}

	@AfterEach
	void tearDown() throws IOException {
		mockWebServer.shutdown();
	}

	@Test
	void loadWeather_success() {
		String body = """
			{
			  "response": {
			    "body": {
			      "items": {
			        "item": [
			          { "category": "T1H", "obsrValue": "23.5" },
			          { "category": "RN1", "obsrValue": "0" },
			          { "category": "PTY", "obsrValue": "1" }
			        ]
			      }
			    }
			  }
			}
			""";

		mockWebServer.enqueue(new MockResponse()
			.setBody(body)
			.setResponseCode(200)
			.addHeader("Content-Type", "application/json"));

		double lat = 37.5665;
		double lon = 126.9780;

		Mono<WeatherResponseDto> result = weatherService.loadWeather(lat, lon);

		StepVerifier.create(result)
			.assertNext(res -> {
				assertThat(res.getTemp()).isEqualTo("23.5℃");
				assertThat(res.getRainAmount()).isEqualTo("강수 없음");
				assertThat(res.getRainStatus()).isEqualTo("비");
			})
			.verifyComplete();
	}
}
