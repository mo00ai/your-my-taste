package com.example.taste.domain.recommend.service;

import static com.example.taste.domain.user.exception.UserErrorCode.*;

import org.springframework.stereotype.Service;

import com.example.taste.common.exception.CustomException;
import com.example.taste.domain.recommend.dto.request.RecommendRequestDto;
import com.example.taste.domain.recommend.dto.response.CoordinateResponseDto;
import com.example.taste.domain.recommend.dto.response.RecommendResponseDto;
import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
public class RecommendService {

	private final AddressService addressService;
	private final WeatherService weatherService;
	private final AiResponseService aiResponseService;
	private final UserRepository userRepository;

	public Mono<RecommendResponseDto> recommend(Long userId, RecommendRequestDto dto) {

		//비동기 사이에서 db에서 데이터를 가져오는 동기를 수행하기 때문에
		//fromCallable, boundedElastic이용
		//동기를 비동기로 사용하기 위해서!

		String message =
			(dto == null || dto.getMessage() == null || dto.getMessage().isBlank()) ? "지금 뭐 먹을까. 메뉴 추천 좀 해줘" :
				dto.getMessage();

		return Mono.fromCallable(() ->
				userRepository.findUserWithFavors(userId)
					.orElseThrow(() -> new CustomException(NOT_FOUND_USER))
			)
			.subscribeOn(Schedulers.boundedElastic())
			.flatMap(user ->
				addressService.getCoordinates(user)
					.flatMap(coordinate -> getRecommendation(coordinate, user, message))
			);
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
}
