package com.example.taste.domain.recommend.service;

import static org.assertj.core.api.AssertionsForClassTypes.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.taste.domain.favor.entity.Favor;
import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.entity.UserFavor;
import com.example.taste.fixtures.UserFixture;
import com.example.taste.property.AbstractIntegrationTest;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest
class AiResponseServiceTest extends AbstractIntegrationTest {

	private MockWebServer mockWebServer;

	@Autowired
	private AiResponseService aiResponseService;

	@BeforeEach
	void setup() throws IOException, NoSuchFieldException, IllegalAccessException {
		mockWebServer = new MockWebServer();
		mockWebServer.start();

		WebClient webClient = WebClient.builder()
			.baseUrl(mockWebServer.url("/").toString())
			.build();

		aiResponseService = new AiResponseService(webClient);

		Field field = AiResponseService.class.getDeclaredField("model");
		field.setAccessible(true);
		field.set(aiResponseService, "gpt-3.5-turbo");
	}

	@AfterEach
	void shutdown() throws IOException {
		mockWebServer.shutdown();
	}

	@Test
	void ai_response_recommendFood_success() {
		// given
		String body =
			"""
					{
					  "choices": [
					    {
					      "message": {
					        "content": "오늘 같은 더운 날엔 시원한 냉면이 제격이에요!"
					      }
					    }
					  ]
					}
				""";

		mockWebServer.enqueue(new MockResponse()
			.setBody(body)
			.setResponseCode(200)
			.addHeader("Content-Type", "application/json"));

		User dummyUser = UserFixture.create(null);

		UserFavor favor = UserFavor.builder()
			.favor(Favor.builder().name("매운맛").build())
			.user(dummyUser)
			.build();

		// when
		Mono<String> result = aiResponseService.recommendFood(
			"점심 뭐 먹을까?",
			List.of(favor),
			"28℃", "0mm", "강수 없음"
		);

		// then
		StepVerifier.create(result)
			.assertNext(content ->
				assertThat(content).contains("냉면") // 내용 일부 확인
			)
			.verifyComplete();
	}
}
