package com.example.taste.domain.store.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.taste.common.exception.CustomException;
import com.example.taste.domain.store.dto.request.AddBucketItemRequest;
import com.example.taste.domain.store.dto.response.StoreBucketResponse;
import com.example.taste.domain.store.entity.Store;
import com.example.taste.domain.store.entity.StoreBucket;
import com.example.taste.domain.store.entity.StoreBucketItem;
import com.example.taste.domain.store.repository.StoreBucketItemRepository;
import com.example.taste.domain.store.repository.StoreBucketRepository;
import com.example.taste.domain.store.repository.StoreRepository;
import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.repository.UserRepository;
import com.example.taste.fixtures.StoreBucketFixture;
import com.example.taste.fixtures.StoreFixture;
import com.example.taste.fixtures.UserFixture;

import jakarta.transaction.Transactional;

@SpringBootTest
class StoreBucketServiceTest {

	@Autowired
	private StoreBucketService storeBucketService;
	@Autowired
	private StoreBucketItemRepository storeBucketItemRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private StoreRepository storeRepository;
	@Autowired
	private StoreBucketRepository storeBucketRepository;

	// 이미지 엔티티 관련 오류 발생 -> 추후 pull 받아서 테스트 실행
	@Test
	void 에러발생시_롤백() {
		// given
		User user1 = userRepository.save(UserFixture.create());
		User user2 = userRepository.save(UserFixture.create());
		Store store = storeRepository.save(StoreFixture.create());
		StoreBucket storeBucket1 = storeBucketRepository.save(StoreBucketFixture.createOpenedBucket(user1));
		StoreBucket storeBucket2 = storeBucketRepository.save(StoreBucketFixture.createOpenedBucket(user2));

		List<Long> bucketIds = List.of(storeBucket1.getId(), storeBucket2.getId());
		AddBucketItemRequest request = new AddBucketItemRequest(store.getId(), bucketIds);

		// when
		assertThrows(CustomException.class, () -> {
			storeBucketService.addBucketItem(request, user1.getId());
		});

		// then
		List<StoreBucketItem> items = storeBucketItemRepository.findAll();
		assertThat(items).isEmpty();
	}

	@Test
	@Transactional
	void 비공개_버킷은_반환X() {
		// given
		User user = userRepository.save(UserFixture.create());
		StoreBucket closedBucket = storeBucketRepository.save(StoreBucketFixture.createClosedBucket(user));
		StoreBucket openBucket = storeBucketRepository.save(StoreBucketFixture.createOpenedBucket(user));

		// when
		List<StoreBucketResponse> responses = storeBucketService.getBucketsByUserId(user.getId());

		// then
		assertThat(responses).hasSize(1);
		assertThat(responses.get(0).getId()).isEqualTo(openBucket.getId());
	}

	@Test
	void getBucketItems() {
	}

	@Test
	void deleteBucket() {
	}

	@Test
	void removeBucketItem() {
	}

	@Test
	void makeUnduplicateName() {
	}
}