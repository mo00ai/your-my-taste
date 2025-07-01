package com.example.taste.domain.map.service;

import static org.assertj.core.api.AssertionsForClassTypes.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.example.taste.domain.map.dto.geocode.GeoMapDetailResponse;
import com.example.taste.property.AbstractIntegrationTest;

@ActiveProfiles("test-int")
@SpringBootTest
public class NaverMapServiceTest extends AbstractIntegrationTest {
	@Autowired
	private NaverMapService naverMapService;

	@DisplayName("주소를 좌표로 변환하는 naver geoCode API 호출 테스트")
	@Test
	void convertAddressToCoordinates_successfully() {
		// given
		String address = "서울특별시 중구 퇴계로 100";

		// when
		GeoMapDetailResponse response = naverMapService.getCoordinatesFromAddress(address);

		// then
		assertThat(response).isNotNull();

		assertThat(response.getAddresses()).isNotNull();
		assertThat(response.getAddresses().size()).isGreaterThan(0);
	}

	// @Test
	// @DisplayName("리버스 지오코딩 성공 - 행정동 주소 반환")
	// void reverseGeocode_success() {
	// 	// given
	// 	String coordinates = "126.9782752,37.5666421";
	//
	// 	// when
	// 	ReverseGeocodeDetailResponse response = naverMapService.getAddressFromStringCoordinates(coordinates);
	//
	// 	// then
	// 	assertThat(response).isNotNull();
	// 	assertThat(response.getResults()).isNotEmpty();
	//
	// 	// 행정동 타입(admcode)만 필터링해서 검증해도 좋음
	// 	var admResult = response.getResults().stream()
	// 		.filter(r -> r.getName().equals("admcode"))
	// 		.findFirst()
	// 		.orElseThrow();
	// 	assertThat(admResult.getRegion().getArea1().getName()).isEqualTo("서울특별시");
	// }
}
