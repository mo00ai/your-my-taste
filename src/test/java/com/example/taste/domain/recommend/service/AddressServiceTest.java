package com.example.taste.domain.recommend.service;

import static org.assertj.core.api.AssertionsForClassTypes.*;

import java.io.IOException;
import java.lang.reflect.Field;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.taste.common.exception.CustomException;
import com.example.taste.domain.recommend.dto.response.CoordinateResponseDto;
import com.example.taste.domain.user.entity.User;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class AddressServiceTest {

	private MockWebServer mockWebServer;
	private AddressService addressService;

	@BeforeEach
	void setup() throws IOException, NoSuchFieldException, IllegalAccessException {
		mockWebServer = new MockWebServer();
		mockWebServer.start();

		WebClient kakaoWebClient = WebClient.builder()
			.baseUrl(mockWebServer.url("/").toString())
			.build();

		addressService = new AddressService(kakaoWebClient);

		//필드 @Value 주입
		Field kakaoKeyField = AddressService.class.getDeclaredField("kakaoKey");
		kakaoKeyField.setAccessible(true);
		kakaoKeyField.set(addressService, "dummyKakaoKey");
	}

	@AfterEach
	void terminate() throws IOException {
		mockWebServer.shutdown();
	}

	@Test
	void getCoordinates_success() throws Exception {
		// given
		String body =
			"""
				{
				  "documents": [
				    {
				      "x": "127.123",
				      "y": "37.456"
				    }
				  ]
				}
				""";
		mockWebServer.enqueue(new MockResponse()
			.setResponseCode(200)
			.setBody(body)
			.addHeader("Content-Type", "application/json"));

		User user = User.builder()
			.address("서울시 강남구")
			.build();

		// when
		Mono<CoordinateResponseDto> result = addressService.getCoordinates(user);

		// then
		StepVerifier.create(result)
			.assertNext(dto -> {
				assertThat(dto.getLon()).isEqualTo(127.123);
				assertThat(dto.getLat()).isEqualTo(37.456);
			})
			.verifyComplete();
	}

	@Test
	void getCoordinates_emptyDocuments_thenThrow() throws Exception {
		// given
		String body =
			"""
				{
				  "documents": []
				}
				""";
		mockWebServer.enqueue(new MockResponse()
			.setResponseCode(200)
			.setBody(body)
			.addHeader("Content-Type", "application/json"));

		User user = User.builder()
			.address("없는주소")
			.build();

		// when
		Mono<CoordinateResponseDto> result = addressService.getCoordinates(user);

		// then
		StepVerifier.create(result)
			.expectError(CustomException.class)
			.verify();
	}
}

