package com.example.taste.domain.store.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.taste.common.exception.CustomException;
import com.example.taste.domain.store.dto.request.AddBucketItemRequest;
import com.example.taste.domain.store.entity.Store;
import com.example.taste.domain.store.entity.StoreBucket;
import com.example.taste.domain.store.entity.StoreBucketItem;
import com.example.taste.domain.store.repository.StoreBucketItemRepository;
import com.example.taste.domain.store.repository.StoreBucketRepository;
import com.example.taste.domain.store.repository.StoreRepository;
import com.example.taste.domain.user.entity.User;

@ExtendWith(MockitoExtension.class)
public class StoreBucketServiceUnitTest {
	@InjectMocks
	private StoreBucketService storeBucketService;

	@Mock
	private StoreRepository storeRepository;

	@Mock
	private StoreBucketRepository storeBucketRepository;

	@Mock
	private StoreBucketItemRepository storeBucketItemRepository;

	@Test
	void 로그인유저_버킷아니면_CustomException() {
		// given
		Long requestUserId = 1L;
		Long bucketOwnerId = 2L;
		Long storeId = 1L;
		Long bucketId = 1L;

		AddBucketItemRequest request = new AddBucketItemRequest(storeId, List.of(bucketId));
		Store store = mock(Store.class);
		StoreBucket storeBucket = mock(StoreBucket.class);
		User user = mock(User.class);

		given(storeRepository.findById(request.getStoreId())).willReturn(Optional.of(store));
		given(storeBucketRepository.findById(bucketId)).willReturn(Optional.of(storeBucket));
		given(storeBucket.getUser()).willReturn(user);
		given(storeBucket.getUser().getId()).willReturn(bucketOwnerId);

		// when, then
		assertThrows(CustomException.class, () -> {
			storeBucketService.addBucketItem(request, requestUserId);
		});
	}

	@Test
	void 버킷에_가게가_존재하면_continue() {
		// given
		Long storeId = 1L;
		Long bucketId = 1L;
		Long userId = 1L;

		AddBucketItemRequest request = new AddBucketItemRequest(storeId, List.of(bucketId));
		Store store = mock(Store.class);
		StoreBucket storeBucket = mock(StoreBucket.class);
		User user = mock(User.class);
		StoreBucketItem storeBucketItem = mock(StoreBucketItem.class);

		given(storeRepository.findById(request.getStoreId())).willReturn(Optional.of(store));
		given(storeBucketRepository.findById(bucketId)).willReturn(Optional.of(storeBucket));
		given(storeBucket.getUser()).willReturn(user);
		given(storeBucket.getUser().getId()).willReturn(userId);
		given(storeBucketItemRepository.findByStoreAndStoreBucket(store, storeBucket)).willReturn(
			Optional.of(storeBucketItem));

		// when
		storeBucketService.addBucketItem(request, userId);

		// then
		verify(storeBucketItemRepository, never()).save(any(StoreBucketItem.class));
	}
}
