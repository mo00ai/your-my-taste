package com.example.taste.domain.store.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.taste.common.exception.CustomException;
import com.example.taste.domain.image.entity.Image;
import com.example.taste.domain.image.repository.ImageRepository;
import com.example.taste.domain.store.dto.request.AddBucketItemRequest;
import com.example.taste.domain.store.dto.request.RemoveBucketItemRequest;
import com.example.taste.domain.store.dto.response.BucketItemResponse;
import com.example.taste.domain.store.dto.response.StoreBucketResponse;
import com.example.taste.domain.store.entity.Category;
import com.example.taste.domain.store.entity.Store;
import com.example.taste.domain.store.entity.StoreBucket;
import com.example.taste.domain.store.entity.StoreBucketItem;
import com.example.taste.domain.store.repository.CategoryRepository;
import com.example.taste.domain.store.repository.StoreBucketItemRepository;
import com.example.taste.domain.store.repository.StoreBucketRepository;
import com.example.taste.domain.store.repository.StoreRepository;
import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.repository.UserRepository;
import com.example.taste.fixtures.CategoryFixture;
import com.example.taste.fixtures.ImageFixture;
import com.example.taste.fixtures.StoreBucketFixture;
import com.example.taste.fixtures.StoreBucketItemFixture;
import com.example.taste.fixtures.StoreFixture;
import com.example.taste.fixtures.UserFixture;

import jakarta.persistence.EntityManager;
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
	@Autowired
	private ImageRepository imageRepository;
	@Autowired
	private CategoryRepository categoryRepository;
	@Autowired
	private EntityManager em;

	@AfterEach
	void cleanUp() {
		storeBucketRepository.deleteAll();
		storeRepository.deleteAll();
		categoryRepository.deleteAll();
		userRepository.deleteAll();
		imageRepository.deleteAll();
	}

	@Test
	void addBucketItem_에러발생시_롤백() {
		// given
		Image image1 = ImageFixture.create();
		Image image2 = ImageFixture.create();

		User user1 = userRepository.save(UserFixture.create(image1));
		User user2 = userRepository.save(UserFixture.createAnother(image2));

		Category category = categoryRepository.save(CategoryFixture.create());
		Store store = storeRepository.save(StoreFixture.create(category));

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
		Image image = ImageFixture.create();
		User user = userRepository.save(UserFixture.create(image));
		StoreBucket closedBucket = storeBucketRepository.save(StoreBucketFixture.createClosedBucket(user));
		StoreBucket openBucket = storeBucketRepository.save(StoreBucketFixture.createOpenedBucket(user));

		// when
		List<StoreBucketResponse> responses = storeBucketService.getBucketsByUserId(user.getId());

		// then
		assertThat(responses).hasSize(1);
		assertThat(responses.get(0).getId()).isEqualTo(openBucket.getId());
	}

	@Test
	@Transactional
	void 본인의_버킷은_비공개여도_접근가능() {
		// given
		Image image = ImageFixture.create();
		User user = userRepository.save(UserFixture.create(image));

		Category category = categoryRepository.save(CategoryFixture.create());
		Store store = storeRepository.save(StoreFixture.create(category));

		StoreBucket closedBucket = storeBucketRepository.save(StoreBucketFixture.createClosedBucket(user));
		em.clear();
		StoreBucketItem item = storeBucketItemRepository.save(StoreBucketItemFixture.create(closedBucket, store));

		// when
		List<BucketItemResponse> responses = storeBucketService.getBucketItems(closedBucket.getId(), user.getId());

		// then
		assertThat(responses).hasSize(1);
		assertThat(responses.get(0).getId()).isEqualTo(item.getId());
	}

	@Test
	@Transactional
	void 타유저의_비공개버킷_조회시_CustomException() {
		// given
		Image image1 = ImageFixture.create();
		Image image2 = ImageFixture.create();
		User user1 = userRepository.save(UserFixture.create(image1));
		User user2 = userRepository.save(UserFixture.createAnother(image2));

		Category category = categoryRepository.save(CategoryFixture.create());
		Store store = storeRepository.save(StoreFixture.create(category));

		StoreBucket closedBucket = storeBucketRepository.save(StoreBucketFixture.createClosedBucket(user1));
		em.clear();
		StoreBucketItem item = storeBucketItemRepository.save(StoreBucketItemFixture.create(closedBucket, store));

		// when, then
		assertThrows(CustomException.class, () -> {
			storeBucketService.getBucketItems(closedBucket.getId(), user2.getId());
		});
	}

	@Test
	@Transactional
	void 버킷_삭제시_버킷아이템도_삭제() {

		// given
		Image image = ImageFixture.create();
		User user = userRepository.save(UserFixture.create(image));

		Category category = categoryRepository.save(CategoryFixture.create());
		Store store = storeRepository.save(StoreFixture.create(category));

		StoreBucket closedBucket = storeBucketRepository.saveAndFlush(StoreBucketFixture.createClosedBucket(user));
		em.clear();
		StoreBucketItem item = storeBucketItemRepository.saveAndFlush(
			StoreBucketItemFixture.create(closedBucket, store));

		// when
		storeBucketService.deleteBucket(closedBucket.getId(), user.getId());

		// then
		assertThat(storeBucketItemRepository.existsById(item.getId())).isFalse();
	}

	@Test
	@Transactional
	void 유효하지않은_버킷아이템이_포함되면_에러() {
		// given
		Category category = categoryRepository.save(CategoryFixture.create());
		Store store = storeRepository.save(StoreFixture.create(category));

		Image image1 = ImageFixture.create();
		Image image2 = ImageFixture.create();
		User user1 = userRepository.save(UserFixture.create(image1));
		User user2 = userRepository.save(UserFixture.createAnother(image2));

		StoreBucket storeBucket1 = storeBucketRepository.save(StoreBucketFixture.createOpenedBucket(user1));
		StoreBucket storeBucket2 = storeBucketRepository.save(StoreBucketFixture.createOpenedBucket(user2));
		em.clear();
		StoreBucketItem item1 = storeBucketItemRepository.save(StoreBucketItemFixture.create(storeBucket1, store));
		StoreBucketItem item2 = storeBucketItemRepository.save(StoreBucketItemFixture.create(storeBucket2, store));

		RemoveBucketItemRequest request = new RemoveBucketItemRequest(List.of(item1.getId(), item2.getId()));

		// when, then
		assertThrows(CustomException.class, () -> {
			storeBucketService.removeBucketItem(storeBucket1.getId(), request, user1.getId());
		});
	}

	@Test
	@Transactional
	void 버킷아이템이_유효하면_remove_성공() {
		// given
		Category category = categoryRepository.save(CategoryFixture.create());
		Store store = storeRepository.save(StoreFixture.create(category));

		Image image = ImageFixture.create();
		User user = userRepository.save(UserFixture.create(image));

		StoreBucket storeBucket = storeBucketRepository.save(StoreBucketFixture.createOpenedBucket(user));
		StoreBucketItem item1 = storeBucketItemRepository.save(StoreBucketItemFixture.create(storeBucket, store));
		StoreBucketItem item2 = storeBucketItemRepository.save(StoreBucketItemFixture.create(storeBucket, store));

		RemoveBucketItemRequest request = new RemoveBucketItemRequest(List.of(item1.getId(), item2.getId()));

		// when
		storeBucketService.removeBucketItem(storeBucket.getId(), request, user.getId());

		// then
		assertThat(storeBucketItemRepository.existsById(item1.getId())).isFalse();
		assertThat(storeBucketItemRepository.existsById(item2.getId())).isFalse();
	}

	// @Test
	// @Transactional
	// void 버킷명이_중복이_아니면_입력값대로_버킷명_반환() {
	// 	// given
	// 	Image image = imageRepository.save(ImageFixture.create());
	// 	User user = userRepository.save(UserFixture.create(image));
	// 	StoreBucket storeBucket = storeBucketRepository.save(StoreBucketFixture.createOpenedBucket(user));
	// 	String newName = "기본 리스트1";
	//
	// 	// when, then
	// 	assertThat(storeBucketService.makeUnduplicateName(newName, user)).isEqualTo(newName);
	// }
}