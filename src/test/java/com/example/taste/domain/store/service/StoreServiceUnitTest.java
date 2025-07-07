package com.example.taste.domain.store.service;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.assertj.core.api.InstanceOfAssertFactories.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.example.taste.common.exception.CustomException;
import com.example.taste.common.response.PageResponse;
import com.example.taste.domain.embedding.dto.StoreSearchCondition;
import com.example.taste.domain.embedding.service.EmbeddingService;
import com.example.taste.domain.map.service.NaverMapService;
import com.example.taste.domain.review.repository.ReviewRepository;
import com.example.taste.domain.searchapi.dto.NaverLocalSearchResponseDto;
import com.example.taste.domain.store.dto.response.StoreResponse;
import com.example.taste.domain.store.dto.response.StoreSearchResult;
import com.example.taste.domain.store.dto.response.StoreSimpleResponseDto;
import com.example.taste.domain.store.entity.Category;
import com.example.taste.domain.store.entity.Store;
import com.example.taste.domain.store.exception.StoreErrorCode;
import com.example.taste.domain.store.repository.CategoryRepository;
import com.example.taste.domain.store.repository.StoreBucketItemRepository;
import com.example.taste.domain.store.repository.StoreRepository;

import jakarta.validation.ConstraintViolationException;

@ExtendWith(MockitoExtension.class)
public class StoreServiceUnitTest {

	@Spy
	@InjectMocks
	private StoreService storeService;
	@Mock
	private StoreRepository storeRepository;
	@Mock
	private CategoryRepository categoryRepository;
	@Mock
	private NaverMapService naverMapService;
	@Mock
	private ReviewRepository reviewRepository;
	@Mock
	private EmbeddingService embeddingService;
	@Mock
	private StoreBucketItemRepository storeBucketItemRepository;

	@DisplayName("가게 저장 성공")
	@Test
	void createStore_success() {
		// given
		NaverLocalSearchResponseDto dto = mock(NaverLocalSearchResponseDto.class);
		Store store = mock(Store.class);

		doReturn(store).when(storeService).toStoreEntity(any(NaverLocalSearchResponseDto.class));
		when(storeRepository.save(store)).thenReturn(store);
		// when
		StoreSimpleResponseDto result = storeService.createStore(dto);

		// then
		assertThat(result).isNotNull();
		verify(storeRepository).save(any(Store.class));
	}

	@DisplayName("가게 저장 실패 - 이미 존재하는 가게")
	@Test
	void createStore_duplicateStore() {
		// given
		NaverLocalSearchResponseDto dto = mock(NaverLocalSearchResponseDto.class);
		Store dummyStore = mock(Store.class);

		doReturn(dummyStore).when(storeService).toStoreEntity(any(NaverLocalSearchResponseDto.class));

		ConstraintViolationException rootCause = new ConstraintViolationException("제약조건 위반", Collections.emptySet());
		DataIntegrityViolationException dbException = new DataIntegrityViolationException("무결성 위반", rootCause);

		when(storeRepository.save(any(Store.class))).thenThrow(dbException);

		// when & then
		assertThatThrownBy(() -> storeService.createStore(dto))
			.isInstanceOf(CustomException.class)
			.satisfies(ex -> {
				CustomException ce = (CustomException)ex;
				assertThat(ce.getBaseCode()).isEqualTo(StoreErrorCode.STORE_ALREADY_EXISTS);
			});

		verify(storeRepository).save(any(Store.class));
	}

	@DisplayName("가게 저장 실패 - 검색 결과가 없는 경우")
	@Test
	void createStore_noSearchResult() {
		// given
		NaverLocalSearchResponseDto dto = mock(NaverLocalSearchResponseDto.class);
		when(dto.getItems()).thenReturn(Collections.emptyList());

		// when & then
		assertThatThrownBy(() -> storeService.createStore(dto))
			.isInstanceOf(CustomException.class)
			.satisfies(ex -> {
				CustomException ce = (CustomException)ex;
				assertThat(ce.getBaseCode()).isEqualTo(StoreErrorCode.STORE_NOT_FOUND);
			});

		verify(dto).getItems();
	}

	@DisplayName("가게 단건 조회 성공")
	@Test
	void getStoreById_success() {
		// given
		Long storeId = 1L;
		Store mockStore = mock(Store.class);
		when(storeRepository.findById(storeId)).thenReturn(Optional.of(mockStore));

		// when
		StoreResponse result = storeService.getStore(storeId);

		// then
		assertThat(result).isNotNull();
		verify(storeRepository).findById(storeId);
	}

	@DisplayName("가게 단건 조회 실패 - 존재하지 않는 가게")
	@Test
	void getStoreById_notFound() {
		// given
		Long storeId = 999L;
		when(storeRepository.findById(storeId)).thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> storeService.getStore(storeId))
			.isInstanceOf(CustomException.class)
			.satisfies(ex -> {
				CustomException ce = (CustomException)ex;
				assertThat(ce.getBaseCode()).isEqualTo(StoreErrorCode.STORE_NOT_FOUND);
			});

		verify(storeRepository).findById(storeId);
	}

	@DisplayName("가게 삭제 성공 - softDelete")
	@Test
	void deleteStore_success() {
		// given
		Long storeId = 1L;
		Store mockStore = mock(Store.class);

		when(storeRepository.findById(storeId)).thenReturn(Optional.of(mockStore));

		// when
		storeService.deleteStore(storeId);

		// then
		verify(storeBucketItemRepository).deleteAllByStore(mockStore);
		verify(mockStore).softDelete();
	}

	@DisplayName("가게 삭제 실패 - 존재하지 않는 가게")
	@Test
	void deleteStore_notFound() {
		// given
		Long storeId = 999L;
		when(storeRepository.findById(storeId)).thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> storeService.deleteStore(storeId))
			.isInstanceOf(CustomException.class)
			.satisfies(ex -> {
				CustomException ce = (CustomException)ex;
				assertThat(ce.getBaseCode()).isEqualTo(StoreErrorCode.STORE_NOT_FOUND);
			});

		verify(storeRepository).findById(storeId);
	}

	@DisplayName("카테고리 조회 성공 - 이미 존재하는 카테고리")
	@Test
	void getOrCreateCategory_alreadyExists() {
		// given
		String categoryName = "한식";
		Category existingCategory = mock(Category.class);

		when(categoryRepository.findByName(categoryName))
			.thenReturn(Optional.of(existingCategory));

		// when
		Category result = storeService.getOrCreateCategory(categoryName);

		// then
		assertThat(result).isEqualTo(existingCategory);
		verify(categoryRepository, never()).save(any());
	}

	@DisplayName("카테고리 생성 성공 - 신규 카테고리")
	@Test
	void getOrCreateCategory_createNew() {
		// given
		String categoryName = "일식";
		Category newCategory = mock(Category.class);

		when(categoryRepository.findByName(categoryName)).thenReturn(Optional.empty());
		when(categoryRepository.findMaxDisplayOrder()).thenReturn(Optional.of(10));
		when(categoryRepository.save(any())).thenReturn(newCategory);

		// when
		Category result = storeService.getOrCreateCategory(categoryName);

		// then
		assertThat(result).isEqualTo(newCategory);
		verify(categoryRepository).save(any());
	}

	@DisplayName("카테고리 생성 실패 - CATEGORY_CREATION_CONFLICT 예외 발생")
	@Test
	void getOrCreateCategory_conflictAndRetryFails() {
		// given
		String categoryName = "디저트";
		// 2번 조회
		when(categoryRepository.findByName(categoryName))
			.thenReturn(Optional.empty())
			.thenReturn(Optional.empty());

		when(categoryRepository.findMaxDisplayOrder()).thenReturn(Optional.of(5));
		when(categoryRepository.save(any())).thenThrow(new DataIntegrityViolationException("중복"));

		// when & then
		assertThatThrownBy(() -> storeService.getOrCreateCategory(categoryName))
			.isInstanceOf(CustomException.class)
			.satisfies(ex -> {
				CustomException ce = (CustomException)ex;
				assertThat(ce.getBaseCode()).isEqualTo(StoreErrorCode.CATEGORY_CREATION_CONFLICT);
			});

		verify(categoryRepository, times(2)).findByName(categoryName);
		verify(categoryRepository).save(any());
	}

	@DisplayName("가게 유사도 검색 성공 - 결과 있음")
	@Test
	void searchStore_success_withResults() {
		// given
		String query = "강남 돈까스 맛집";
		double threshold = 0.5;
		StoreSearchCondition condition = StoreSearchCondition.builder()
			.query(query)
			.similarityThreshold(threshold)
			.build();
		Pageable pageable = PageRequest.of(0, 10);

		float[] mockEmbedding = new float[1536];
		Arrays.fill(mockEmbedding, 0.3f);

		StoreSearchResult result1 = mock(StoreSearchResult.class);
		StoreSearchResult result2 = mock(StoreSearchResult.class);

		Page<StoreSearchResult> page = new PageImpl<>(List.of(result1, result2));

		// when(embeddingService.search(query)).thenReturn(mockEmbedding);
		when(embeddingService.search(anyString())).thenReturn(mockEmbedding);
		when(storeRepository.searchByVector(mockEmbedding, threshold, pageable)).thenReturn(page);

		// when
		PageResponse<StoreSearchResult> response = storeService.searchStore(condition, pageable);

		// then
		assertThat(response).isNotNull();
		assertThat(response.getContent()).asList().hasSize(2);
		verify(embeddingService).search(query);
		verify(storeRepository).searchByVector(mockEmbedding, threshold, pageable);
	}

	@DisplayName("가게 유사도 검색 성공 - 결과 없음")
	@Test
	void searchStore_success_withNoResults() {
		// given
		String query = "강남 수제버거";
		double threshold = 0.7;
		StoreSearchCondition condition = StoreSearchCondition.builder()
			.query(query)
			.similarityThreshold(threshold)
			.build();
		Pageable pageable = PageRequest.of(0, 10);

		float[] mockEmbedding = new float[1536];
		Arrays.fill(mockEmbedding, 0.4f);

		Page<StoreSearchResult> emptyPage = new PageImpl<>(Collections.emptyList());

		when(embeddingService.search(query)).thenReturn(mockEmbedding);
		when(storeRepository.searchByVector(mockEmbedding, threshold, pageable)).thenReturn(emptyPage);

		// when
		PageResponse<StoreSearchResult> response = storeService.searchStore(condition, pageable);

		// then
		assertThat(response).isNotNull();
		List<StoreSearchResult> content = response.getContent();
		assertThat(content)
			.asInstanceOf(list(StoreSearchResult.class))
			.isEmpty();
		verify(embeddingService).search(query);
		verify(storeRepository).searchByVector(mockEmbedding, threshold, pageable);
	}

	@DisplayName("검색 조건 default값 확인")
	@Test
	void storeSearchCondition_defaultValues() {
		StoreSearchCondition condition = new StoreSearchCondition();
		condition.setQuery("마라탕");

		assertThat(condition.getRadius()).isEqualTo(3000);
		assertThat(condition.getSimilarityThreshold()).isEqualTo(0.7);
		assertThat(condition.getLatitude()).isEqualTo("37.5548376");
		assertThat(condition.getLongitude()).isEqualTo("126.9717326");
		assertThat(condition.hasGeo()).isTrue();
	}

	@DisplayName("가게 임베딩 텍스트 생성")
	@Test
	void buildStoreEmbeddingText_basicTest() {
		// given
		Store store = Store.builder()
			.name("강남 돈까스")
			.sido("서울특별시")
			.sigungu("강남구")
			.eupmyeondong("역삼동")
			.category(Category.builder().name("일식").build())
			.build();

		// when
		String text = storeService.buildStoreEmbeddingText(store);

		// then
		assertThat(text).contains("서울특별시", "강남구", "역삼동", "강남 돈까스", "일식");
	}

}
