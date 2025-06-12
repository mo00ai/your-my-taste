package com.example.taste.domain.store.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.taste.common.exception.CustomException;
import com.example.taste.common.util.EntityFetcher;
import com.example.taste.domain.image.entity.Image;
import com.example.taste.domain.store.dto.request.AddBucketItemRequest;
import com.example.taste.domain.store.entity.Store;
import com.example.taste.domain.store.entity.StoreBucket;
import com.example.taste.domain.store.entity.StoreBucketItem;
import com.example.taste.domain.store.repository.StoreBucketItemRepository;
import com.example.taste.domain.store.repository.StoreBucketRepository;
import com.example.taste.domain.user.entity.User;
import com.example.taste.fixtures.ImageFixture;
import com.example.taste.fixtures.StoreBucketFixture;
import com.example.taste.fixtures.UserFixture;

@ExtendWith(MockitoExtension.class)
public class StoreBucketServiceUnitTest {
	@InjectMocks
	private StoreBucketService storeBucketService;

	@Mock
	private StoreBucketRepository storeBucketRepository;

	@Mock
	private StoreBucketItemRepository storeBucketItemRepository;

	@Mock
	private EntityFetcher entityFetcher;

	@Test
	void addBucketItem_whenUserNotBucketOwner_thenThrowException() {
		// given
		Long requestUserId = 1L;
		Long storeId = 1L;
		Long bucketId = 1L;

		AddBucketItemRequest request = new AddBucketItemRequest(storeId, List.of(bucketId));
		Store store = mock(Store.class);
		StoreBucket storeBucket = mock(StoreBucket.class);
		User user = mock(User.class);

		given(entityFetcher.getStoreOrThrow(request.getStoreId())).willReturn(store);
		given(entityFetcher.getStoreBucketOrThrow(bucketId)).willReturn(storeBucket);
		given(storeBucket.getUser()).willReturn(user);
		given(storeBucket.getUser().isSameUser(requestUserId)).willReturn(false);

		// when, then
		assertThrows(CustomException.class, () -> {
			storeBucketService.addBucketItem(request, requestUserId);
		});
	}

	@Test
	void addBucketItem_whenStoreExistsInBucket_thenDoNotSave() {
		// given
		Long storeId = 1L;
		Long bucketId = 1L;
		Long userId = 1L;

		AddBucketItemRequest request = new AddBucketItemRequest(storeId, List.of(bucketId));
		Store store = mock(Store.class);
		StoreBucket storeBucket = mock(StoreBucket.class);
		User user = mock(User.class);

		given(entityFetcher.getStoreOrThrow(request.getStoreId())).willReturn(store);
		given(entityFetcher.getStoreBucketOrThrow(bucketId)).willReturn(storeBucket);
		given(storeBucket.getUser()).willReturn(user);
		given(storeBucket.getUser().isSameUser(userId)).willReturn(true);
		given(storeBucketItemRepository.existsByStoreBucketAndStore(storeBucket, store)).willReturn(true);

		// when
		storeBucketService.addBucketItem(request, userId);

		// then
		verify(storeBucketItemRepository, never()).save(any(StoreBucketItem.class));
	}

	@Test
	void makeUnduplicateName_whenNoBucketNameStartWithInput_thenReturnInput() {
		// given
		String newName = "맛집";
		Image image = ImageFixture.create();
		User user = UserFixture.create(image);

		// stub
		given(storeBucketRepository.findByUserAndNameStartingWith(user, newName)).willReturn(new ArrayList<>());

		// when, then
		assertThat(storeBucketService.makeUnduplicateName(newName, user)).isEqualTo(newName);
	}

	@Test
	void makeUnduplicateName_whenInputHasRegexSymbol_thenReturnInput() {
		// given
		String newName = "맛집+";
		Image image = ImageFixture.create();
		User user = UserFixture.create(image);
		List<StoreBucket> buckets = new ArrayList<>();
		buckets.add(StoreBucketFixture.createOpenedBucketWithName(user, "맛집집"));

		// stub
		given(storeBucketRepository.findByUserAndNameStartingWith(user, newName)).willReturn(new ArrayList<>());

		// when, then
		assertThat(storeBucketService.makeUnduplicateName(newName, user)).isEqualTo(newName);
	}

	@Test
	void makeUnduplicateName_whenInputHasWordInParenthesis_thenReturnInput() {
		// given
		String newName = "맛집(a)";
		Image image = ImageFixture.create();
		User user = UserFixture.create(image);
		List<StoreBucket> buckets = new ArrayList<>();
		buckets.add(StoreBucketFixture.createOpenedBucketWithName(user, "맛집(1)"));

		// stub
		given(storeBucketRepository.findByUserAndNameStartingWith(user, newName)).willReturn(new ArrayList<>());

		// when, then
		assertThat(storeBucketService.makeUnduplicateName(newName, user)).isEqualTo(newName);
	}

	@Test
	void makeUnduplicateName_whenNameDuplicate_thenReturnNameWithSuffix() {
		// given
		String newName = "맛집";
		Image image = ImageFixture.create();
		User user = UserFixture.create(image);
		List<StoreBucket> buckets = new ArrayList<>();
		buckets.add(StoreBucketFixture.createOpenedBucketWithName(user, "맛집"));

		// stub
		given(storeBucketRepository.findByUserAndNameStartingWith(user, newName)).willReturn(buckets);

		// when, then
		assertThat(storeBucketService.makeUnduplicateName(newName, user)).isEqualTo(newName + "(1)");
	}

	@Test
	void makeUnduplicateName_whenMultipleDuplicates_thenIncrementSuffix() {
		// given
		String newName = "맛집";
		Image image = ImageFixture.create();
		User user = UserFixture.create(image);
		List<StoreBucket> buckets = new ArrayList<>();
		buckets.add(StoreBucketFixture.createOpenedBucketWithName(user, "맛집"));
		buckets.add(StoreBucketFixture.createOpenedBucketWithName(user, "맛집(1)"));

		// stub
		given(storeBucketRepository.findByUserAndNameStartingWith(user, newName)).willReturn(buckets);

		// when, then
		assertThat(storeBucketService.makeUnduplicateName(newName, user)).isEqualTo(newName + "(2)");
	}

	@Test
	void makeUnduplicateName_whenBucketsExistWithSuffixNotEquals_thenReturnInput() {
		// given
		String newName = "맛집";
		Image image = ImageFixture.create();
		User user = UserFixture.create(image);
		List<StoreBucket> buckets = new ArrayList<>();
		buckets.add(StoreBucketFixture.createOpenedBucketWithName(user, "맛집(1)"));

		// stub
		given(storeBucketRepository.findByUserAndNameStartingWith(user, newName)).willReturn(buckets);

		// when, then
		assertThat(storeBucketService.makeUnduplicateName(newName, user)).isEqualTo(newName);
	}

	@Test
	void makeUnduplicateName_whenSuffixIsMaxValue_thenThrowException() {
		// given
		String newName = "맛집";
		Image image = ImageFixture.create();
		User user = UserFixture.create(image);
		List<StoreBucket> buckets = List.of(
			StoreBucketFixture.createOpenedBucketWithName(user, "맛집"),
			StoreBucketFixture.createOpenedBucketWithName(user, "맛집(" + Integer.MAX_VALUE + ")")
		);

		given(storeBucketRepository.findByUserAndNameStartingWith(user, newName)).willReturn(buckets);

		// when, then
		assertThrows(CustomException.class, () -> {
			storeBucketService.makeUnduplicateName(newName, user);
		});
	}

	@Test
	void makeUnduplicateName_whenManyDuplicates_thenIncrementSuffix() {
		// given
		String newName = "맛집";
		Image image = ImageFixture.create();
		User user = UserFixture.create(image);
		List<StoreBucket> buckets = new ArrayList<>();
		buckets.add(StoreBucketFixture.createOpenedBucketWithName(user, "맛집"));
		for (int i = 1; i <= 1000; i++) {
			buckets.add(StoreBucketFixture.createOpenedBucketWithName(user, "맛집(" + i + ")"));
		}

		given(storeBucketRepository.findByUserAndNameStartingWith(user, newName)).willReturn(buckets);

		// when, then
		assertThat(storeBucketService.makeUnduplicateName(newName, user)).isEqualTo(newName + "(1001)");
	}

	@Test
	void makeUnduplicateName_whenInputHasSuffixAndDuplicate_thenIncrementSuffix() {
		// given
		String newName = "맛집(1)";
		String baseName = newName.replaceFirst("\\((\\d+)\\)$", "");
		Image image = ImageFixture.create();
		User user = UserFixture.create(image);
		List<StoreBucket> buckets = new ArrayList<>();
		buckets.add(StoreBucketFixture.createOpenedBucketWithName(user, "맛집"));
		buckets.add(StoreBucketFixture.createOpenedBucketWithName(user, "맛집(1)"));

		given(storeBucketRepository.findByUserAndNameStartingWith(user, baseName)).willReturn(buckets);

		// when, then
		assertThat(storeBucketService.makeUnduplicateName(newName, user)).isEqualTo(baseName + "(2)");
	}

	@Test
	void makeUnduplicateName_whenInputHasSuffixAndBaseNameNotExists_thenIncrementSuffix() {
		// given
		String newName = "맛집(1)";
		String baseName = newName.replaceFirst("\\((\\d+)\\)$", "");
		Image image = ImageFixture.create();
		User user = UserFixture.create(image);
		List<StoreBucket> buckets = new ArrayList<>();
		buckets.add(StoreBucketFixture.createOpenedBucketWithName(user, "맛집(1)"));

		given(storeBucketRepository.findByUserAndNameStartingWith(user, baseName)).willReturn(buckets);

		// when, then
		assertThat(storeBucketService.makeUnduplicateName(newName, user)).isEqualTo(baseName + "(2)");
	}
}
