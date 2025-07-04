package com.example.taste.domain.recommend.service;

import static com.example.taste.common.constant.RabbitConst.*;
import static com.example.taste.domain.recommend.exception.RecommendErrorCode.*;
import static com.example.taste.domain.user.exception.UserErrorCode.*;

import java.util.Properties;

import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.stereotype.Service;

import com.example.taste.common.exception.CustomException;
import com.example.taste.domain.recommend.dto.request.RecommendRequestDto;
import com.example.taste.domain.recommend.dto.response.CoordinateResponseDto;
import com.example.taste.domain.recommend.dto.response.RecommendResponseDto;
import com.example.taste.domain.recommend.rabbitmq.ErrorMonitoringProducer;
import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.repository.UserRepository;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendService {

	private final AddressService addressService;
	private final WeatherService weatherService;
	private final AiResponseService aiResponseService;
	private final UserRepository userRepository;
	private final ErrorMonitoringProducer errorMonitoringProducer;

	private static final long MAX_ERROR_QUEUE_THRESHOLD = 100; // 임계치

	private final RabbitAdmin rabbitAdmin;

	@CircuitBreaker(name = "RecommendService", fallbackMethod = "fallbackMethod")
	public Mono<RecommendResponseDto> recommend(Long userId, RecommendRequestDto dto) {

		//에러 큐 길이 확인
		Properties queueProps = rabbitAdmin.getQueueProperties(ERROR_QUEUE_NAME);
		int queueLength = queueProps != null && queueProps.get("QUEUE_MESSAGE_COUNT") != null
			? (Integer)queueProps.get("QUEUE_MESSAGE_COUNT") : 0;

		log.info("[추천 서비스] 현재 큐 길이: {}", queueLength);

		//임계치 초과 시 → 바로 오류 응답
		if (queueLength >= MAX_ERROR_QUEUE_THRESHOLD) {
			return Mono.error(new CustomException(TOO_MANY_API_REQUESTS)); // 429 등 정의된 에러
		}

		//비동기 사이에서 db에서 데이터를 가져오는 동기를 수행하기 때문에
		//fromCallable, boundedElastic이용
		//동기를 비동기로 사용하기 위해서!
		String message =
			(dto == null || dto.getMessage() == null || dto.getMessage().isBlank()) ? "지금 뭐 먹을까. 메뉴 추천 좀 해줘" :
				dto.getMessage();

		return Mono.fromCallable(() ->
				userRepository.findUserWithFavors(userId)
					.orElseThrow(() -> new CustomException(NOT_FOUND_USER)))
			.subscribeOn(Schedulers.boundedElastic())
			.flatMap(user ->
				addressService.getCoordinates(user)
					.flatMap(coord -> getRecommendation(coord, user, message)))
			.doOnError(e -> errorMonitoringProducer.send(userId, message));
	}

	private Mono<RecommendResponseDto> getRecommendation(CoordinateResponseDto coord, User user, String message) {
		return weatherService.loadWeather(coord.getLat(), coord.getLon())
			.flatMap(weather -> aiResponseService.recommendFood(
						message,
						user.getUserFavorList(),
						weather.getTemp(),
						weather.getRainAmount(),
						weather.getRainStatus()
					)
					.map(RecommendResponseDto::new)
			);
	}

	public Mono<RecommendResponseDto> fallbackMethod(Long userId, RecommendRequestDto dto, Throwable throwable) {

		RecommendResponseDto fallbackResponse = RecommendResponseDto.builder()
			.recommend("현재 추천 서비스를 이용할 수 없습니다. 잠시 후 다시 시도해 주세요.")
			.build();

		return Mono.just(fallbackResponse);
	}

}
