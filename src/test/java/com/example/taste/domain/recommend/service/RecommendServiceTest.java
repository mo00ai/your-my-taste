// package com.example.taste.domain.recommend.service;
//
// import static com.example.taste.common.constant.RabbitConst.*;
// import static com.example.taste.domain.recommend.exception.RecommendErrorCode.*;
// import static org.assertj.core.api.AssertionsForClassTypes.*;
// import static org.mockito.BDDMockito.*;
//
// import java.util.Optional;
// import java.util.Properties;
//
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;
// import org.springframework.amqp.rabbit.core.RabbitAdmin;
//
// import com.example.taste.common.exception.CustomException;
// import com.example.taste.domain.recommend.dto.request.RecommendRequestDto;
// import com.example.taste.domain.recommend.dto.response.CoordinateResponseDto;
// import com.example.taste.domain.recommend.dto.response.RecommendResponseDto;
// import com.example.taste.domain.recommend.dto.response.WeatherResponseDto;
// import com.example.taste.domain.recommend.rabbitmq.ErrorMonitoringProducer;
// import com.example.taste.domain.user.entity.User;
// import com.example.taste.domain.user.repository.UserRepository;
// import com.example.taste.fixtures.UserFixture;
// import com.example.taste.property.AbstractIntegrationTest;
//
// import reactor.core.publisher.Mono;
// import reactor.test.StepVerifier;
//
// @ExtendWith(MockitoExtension.class)
// class RecommendServiceTest extends AbstractIntegrationTest {
//
// 	@InjectMocks
// 	private RecommendService recommendService;
//
// 	@Mock
// 	private AddressService addressService;
// 	@Mock
// 	private WeatherService weatherService;
// 	@Mock
// 	private AiResponseService aiResponseService;
// 	@Mock
// 	private UserRepository userRepository;
// 	@Mock
// 	private ErrorMonitoringProducer errorMonitoringProducer;
// 	@Mock
// 	private RabbitAdmin rabbitAdmin;
//
// 	@Test
// 	void recommend_shouldReturnRecommendResponseDto_whenAllServicesSucceed() {
// 		// given
// 		Long userId = 1L;
// 		RecommendRequestDto dto = new RecommendRequestDto("뭐 먹을까?");
//
// 		User user = UserFixture.create(null);
//
// 		CoordinateResponseDto coord = CoordinateResponseDto.builder().lat(37.5).lon(127.0).build();
// 		WeatherResponseDto weather = WeatherResponseDto.builder()
// 			.temp("25℃").rainAmount("강수 없음").rainStatus("맑음")
// 			.build();
// 		String aiResult = "오늘 같은 날엔 냉면이 어울려요!";
//
// 		given(rabbitAdmin.getQueueProperties(ERROR_QUEUE_NAME))
// 			.willReturn(new Properties() {{
// 				put("QUEUE_MESSAGE_COUNT", 10);
// 			}});
//
// 		given(userRepository.findUserWithFavors(userId))
// 			.willReturn(Optional.of(user));
// 		given(addressService.getCoordinates(user))
// 			.willReturn(Mono.just(coord));
// 		given(weatherService.loadWeather(37.5, 127.0))
// 			.willReturn(Mono.just(weather));
// 		given(aiResponseService.recommendFood(
// 			anyString(), anyList(), anyString(), anyString(), anyString()))
// 			.willReturn(Mono.just(aiResult));
//
// 		// when
// 		Mono<RecommendResponseDto> result = recommendService.recommend(userId, dto);
//
// 		// then
// 		StepVerifier.create(result)
// 			.assertNext(res -> assertThat(res.getRecommend()).isEqualTo(aiResult))
// 			.verifyComplete();
// 	}
//
// 	@Test
// 	void recommend_shouldReturnError_whenQueueLengthExceedsThreshold() {
// 		// given
// 		Long userId = 1L;
// 		RecommendRequestDto dto = new RecommendRequestDto("추천해줘");
//
// 		given(userRepository.findUserWithFavors(userId))
// 			.willReturn(Optional.of(UserFixture.create(null)));
//
// 		//레빗엠큐 임계치 도달
// 		given(rabbitAdmin.getQueueProperties(ERROR_QUEUE_NAME))
// 			.willReturn(new Properties() {{
// 				put("QUEUE_MESSAGE_COUNT", 100);
// 			}});
//
// 		// when
// 		Mono<RecommendResponseDto> result = recommendService.recommend(userId, dto);
//
// 		// then
// 		StepVerifier.create(result)
// 			.expectErrorSatisfies(e -> {
// 				assertThat(e).isInstanceOf(CustomException.class);
// 				CustomException ce = (CustomException)e;
// 				assertThat(((CustomException)e).getBaseCode()).isEqualTo(TOO_MANY_API_REQUESTS);
// 				assertThat(((CustomException)e).getBaseCode().getMessage())
// 					.isEqualTo("동시 요청자 수가 많아 요청이 지연되고 있습니다. 다음에 다시 이용해주세요.");
// 			})
// 			.verify();
// 	}
// }
