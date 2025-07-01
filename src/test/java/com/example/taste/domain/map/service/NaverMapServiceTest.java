package com.example.taste.domain.map.service;

import static org.assertj.core.api.AssertionsForClassTypes.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.example.taste.domain.map.dto.geocode.GeoMapDetailResponse;
import com.example.taste.property.AbstractIntegrationTest;

@SpringBootTest
@Transactional
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
}
