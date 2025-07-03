package com.example.taste.domain.map.service;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.BDDMockito.*;

import java.net.URI;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.taste.config.NaverConfig;
import com.example.taste.domain.map.dto.geocode.GeoAddress;
import com.example.taste.domain.map.dto.geocode.GeoMapDetailResponse;
import com.example.taste.domain.map.dto.reversegeocode.ReverseGeocodeArea;
import com.example.taste.domain.map.dto.reversegeocode.ReverseGeocodeDetailResponse;
import com.example.taste.domain.map.dto.reversegeocode.ReverseGeocodeRegion;
import com.example.taste.domain.map.dto.reversegeocode.ReverseGeocodeResult;
import com.example.taste.domain.searchapi.dto.NaverLocalSearchResponseDto;

import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
public class NaverMapServiceUnitTest {

	@InjectMocks
	private NaverMapService naverMapService;

	@Mock
	private WebClient webClient;
	@Mock
	@SuppressWarnings("rawtypes")
	private WebClient.RequestHeadersUriSpec uriSpec;
	@Mock
	private WebClient.RequestBodySpec bodySpec;
	@SuppressWarnings("rawtypes")
	@Mock
	private WebClient.RequestHeadersSpec headersSpec;
	@Mock
	private WebClient.ResponseSpec responseSpec;

	private final NaverConfig naverConfig = new NaverConfig(
		"dummy-client-id",
		"dummy-client-secret",
		new NaverConfig.Geocoding("https://dummy-map-url.com"),
		new NaverConfig.ReverseGeocoding("https://dummy-reverse-url.com")
	);

	@DisplayName("주소를 좌표로 변환하는 naver geoCode API 호출 테스트")
	@Test
	void convertAddressToCoordinates_successfully() {
		// given
		String address = "서울 중구 세종대로 110 서울특별시청";
		Double x = 126.9783882;
		Double y = 37.5666103;

		GeoMapDetailResponse dummyResponse = GeoMapDetailResponse.builder()
			.addresses(List.of(GeoAddress.builder()
				.longitude(Double.toString(x))
				.latitude(Double.toString(y))
				.build()))
			.build();

		// mock WebClient 체인
		given(webClient.get()).willReturn(uriSpec);
		given(uriSpec.uri(any(URI.class))).willReturn(headersSpec);
		given(headersSpec.header(anyString(), anyString())).willReturn(headersSpec);
		given(headersSpec.retrieve()).willReturn(responseSpec);
		given(responseSpec.bodyToMono(GeoMapDetailResponse.class)).willReturn(Mono.just(dummyResponse));

		ReflectionTestUtils.setField(naverMapService, "naverConfig", naverConfig);

		// when
		GeoMapDetailResponse result = naverMapService.getCoordinatesFromAddress(address);

		// then
		assertThat(result).isNotNull();
		assertThat(String.valueOf(result.getAddresses().get(0).getLongitude()))
			.isEqualTo(Double.toString(x));
		assertThat(String.valueOf(result.getAddresses().get(0).getLatitude()))
			.isEqualTo(Double.toString(y));
	}

	@Test
	@DisplayName("리버스 지오코딩 성공 - 행정동 주소 반환")
	void reverseGeocode_success() {
		// given
		String coordinates = "126.9782752,37.5666421";

		// 행정동 주소 구성
		ReverseGeocodeArea area1 = ReverseGeocodeArea.builder()
			.name("서울특별시")
			.build();

		ReverseGeocodeRegion region = ReverseGeocodeRegion.builder()
			.area1(area1)
			.build();

		ReverseGeocodeResult result = ReverseGeocodeResult.builder()
			.name("admcode")
			.region(region)
			.build();

		ReverseGeocodeDetailResponse dummyResponse = ReverseGeocodeDetailResponse.builder()
			.results(List.of(result))
			.build();

		// WebClient mock 체인 구성
		given(webClient.get()).willReturn(uriSpec);
		given(uriSpec.uri(any(URI.class))).willReturn(headersSpec);
		given(headersSpec.header(anyString(), anyString())).willReturn(headersSpec);
		given(headersSpec.retrieve()).willReturn(responseSpec);
		given(responseSpec.bodyToMono(ReverseGeocodeDetailResponse.class)).willReturn(Mono.just(dummyResponse));
		ReflectionTestUtils.setField(naverMapService, "naverConfig", naverConfig);

		// when
		ReverseGeocodeDetailResponse response = naverMapService.getAddressFromStringCoordinates(coordinates);

		// then
		assertThat(response).isNotNull();

		ReverseGeocodeResult admcodeResult = response.getResults().stream()
			.filter(r -> r.getName().equals("admcode"))
			.findFirst()
			.orElseThrow();

		assertThat(admcodeResult.getRegion().getArea1().getName()).isEqualTo("서울특별시");
	}

	private NaverLocalSearchResponseDto createDummyResponse(Double x, Double y) {
		List<NaverLocalSearchResponseDto.Item> items = (x != null && y != null)
			? List.of(NaverLocalSearchResponseDto.Item.builder()
			.mapx(Double.toString(x))
			.mapy(Double.toString(y))
			.build())
			: List.of();

		return NaverLocalSearchResponseDto.builder()
			.items(items)
			.build();
	}
}
