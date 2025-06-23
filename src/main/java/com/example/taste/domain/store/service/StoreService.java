package com.example.taste.domain.store.service;

import static com.example.taste.domain.store.exception.StoreErrorCode.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

import org.springframework.core.NestedExceptionUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.taste.common.exception.CustomException;
import com.example.taste.common.exception.ErrorCode;
import com.example.taste.common.util.EntityFetcher;
import com.example.taste.domain.map.dto.reversegeocode.ReverseGeocodeDetailResponse;
import com.example.taste.domain.map.dto.reversegeocode.ReverseGeocodeRegion;
import com.example.taste.domain.map.dto.reversegeocode.ReverseGeocodeResult;
import com.example.taste.domain.map.service.NaverMapService;
import com.example.taste.domain.review.repository.ReviewRepository;
import com.example.taste.domain.searchapi.dto.NaverLocalSearchResponseDto;
import com.example.taste.domain.store.dto.response.StoreResponse;
import com.example.taste.domain.store.dto.response.StoreSimpleResponseDto;
import com.example.taste.domain.store.entity.Category;
import com.example.taste.domain.store.entity.Store;
import com.example.taste.domain.store.exception.StoreErrorCode;
import com.example.taste.domain.store.repository.CategoryRepository;
import com.example.taste.domain.store.repository.StoreBucketItemRepository;
import com.example.taste.domain.store.repository.StoreRepository;

import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StoreService {
	private final EntityFetcher entityFetcher;
	private final StoreRepository storeRepository;
	private final CategoryRepository categoryRepository;
	private final StoreBucketItemRepository storeBucketItemRepository;
	private final ReviewRepository reviewRepository;
	private final NaverMapService naverMapService;

	@Transactional
	public StoreSimpleResponseDto createStore(NaverLocalSearchResponseDto naverLocalSearchResponseDto) {
		try {
			Store saved = storeRepository.save(toStoreEntity(naverLocalSearchResponseDto));
			return new StoreSimpleResponseDto(saved);
		} catch (DataIntegrityViolationException e) {    // 데이터의 삽입/수정이 무결성 제약 조건을 위반(Spring이 제공하는 상위 무결성 예외)
			Throwable root = NestedExceptionUtils.getRootCause(e);    // 예외의 최하위 원인을 안전하게 추출
			if (root instanceof ConstraintViolationException) {    // 제약 조건이 위배(Hibernate가 DB에서 던지는 하위 예외)
				throw new CustomException(STORE_ALREADY_EXISTS);
			}
			throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
		}

	}

	public StoreResponse getStore(Long id) {
		Store store = entityFetcher.getStoreOrThrow(id);

		List<String> imageUrls = reviewRepository.findTop3OrderByCreatedAtDesc(store).stream()
			.map(review -> review.getImage().getUrl())
			.toList();

		return StoreResponse.create(store, imageUrls); // TODO 가게 정보에 리뷰 이미지 없을때/3개 미만/4개 이상일때 결과값 테스트코드 @김채진
	}

	@Transactional
	public void deleteStore(Long id) {
		Store store = entityFetcher.getStoreOrThrow(id);
		storeBucketItemRepository.deleteAllByStore(store); // NOTE 유저에게 알림? @김채진
		store.softDelete();
	}

	@Transactional(readOnly = true)
	public Store findById(Long storeId) {
		return entityFetcher.getStoreOrThrow(storeId);
	}

	// 카테고리명 추출
	private String extractCategory(String input) {
		String[] tokens = input.split(">" );
		for (int i = 0; i < tokens.length; i++) {
			if (tokens[i].contains("음식점" )) {
				if (i + 1 < tokens.length) {
					return tokens[i + 1];
				}
				break;
			}
		}
		return tokens[0];
	}

	private Store toStoreEntity(NaverLocalSearchResponseDto dto) {
		// 결과 리스트 유효성 검사
		if (dto.getItems().isEmpty()) {
			throw new CustomException(STORE_NOT_FOUND);
		}
		NaverLocalSearchResponseDto.Item item = dto.getItems().get(0);
		// 좌표 정보 -> 네이버 지도 api -> 주소(행정동) 반환
		ReverseGeocodeDetailResponse addressFromStringCoordinates = naverMapService.getAddressFromStringCoordinates(
			getCoordinate(item));
		// 행정동 주소 추출
		Map<String, String> extractedArea = extractAdministrativeArea(addressFromStringCoordinates);
		System.out.println("extractedArea = " + extractedArea);
		// TODO  쿼리 파싱?

		// 카테고리명 추출
		String categoryName = extractCategory(item.getCategory());
		// 카테고리 저장 or 조회
		Category category = getOrCreateCategory(categoryName);
		// 태그 제외한 가게명
		String storeName = stripHtmlTags(item.getTitle());
		BigDecimal longitude = new BigDecimal(item.getMapx()).divide(new BigDecimal("10000000" ), 7,
			RoundingMode.HALF_UP);
		BigDecimal latitude = new BigDecimal(item.getMapy()).divide(new BigDecimal("10000000" ), 7,
			RoundingMode.HALF_UP);
		if (storeRepository.existsByNameAndMapxAndMapy(storeName, longitude, latitude)) {
			throw new CustomException(STORE_ALREADY_EXISTS);
		}
		return Store.builder()
			.category(category)
			.name(stripHtmlTags(item.getTitle()))
			.address(item.getAddress())
			.roadAddress(item.getRoadAddress())
			.mapx(longitude)
			.mapy(latitude)
			.build();

	}

	// 모든 HTML 태그 제거
	private String stripHtmlTags(String input) {
		if (input == null)
			return null;
		return input.replaceAll("<[^>]*>", "" );
	}

	@Transactional
	public Category getOrCreateCategory(String name) {
		// 카테고리 있다면 바로 가져오고
		return categoryRepository.findByName(name)
			.orElseGet(() -> {    // 없다면 생성
				try {
					Integer maxOrder = categoryRepository.findMaxDisplayOrder().orElse(0);
					Category newCategory = Category.builder()
						.name(name)
						.displayOrder(maxOrder + 1)
						.build();
					return categoryRepository.save(newCategory);
				} catch (DataIntegrityViolationException e) { // 데이터 무결성 제약 조건을 위반했을 때 발생하는 에러
					// 이미 다른 트랜잭션에서 INSERT 했을 가능성 → 다시 조회
					return categoryRepository.findByName(name)
						.orElseThrow(() -> new CustomException(CATEGORY_CREATION_CONFLICT));
				}

			});
	}

	private String getCoordinate(NaverLocalSearchResponseDto.Item item) {
		return item.getMapx() + "," + item.getMapy();
	}

	// reverse geocoding -> 행정동 주소 추출
	private Map<String, String> extractAdministrativeArea(ReverseGeocodeDetailResponse response) {
		// "admcode" 타입 결과만 사용 (보통 이게 행정동 기준)
		ReverseGeocodeResult admResult = response.getResults().stream()
			.filter(result -> "admcode".equals(result.getName()))
			.findFirst()
			.orElseThrow(() -> new CustomException(StoreErrorCode.ADMCODE_NOT_FOUND));

		ReverseGeocodeRegion region = admResult.getRegion();

		String sido = region.getArea1().getName();
		String sigungu = region.getArea2().getName();
		String eupmyeondong = region.getArea3().getName();

		return Map.of(
			"sido", sido,    // 시/도
			"sigungu", sigungu,    // 시/군/구
			"eupmyeondong", eupmyeondong // 읍/면/동
		);
	}
}
