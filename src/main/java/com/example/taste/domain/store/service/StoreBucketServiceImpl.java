package com.example.taste.domain.store.service;

import static com.example.taste.domain.store.exception.StoreErrorCode.*;

import org.springframework.stereotype.Service;

import com.example.taste.common.exception.CustomException;
import com.example.taste.domain.store.dto.request.AddBucketItemRequest;
import com.example.taste.domain.store.dto.request.CreateBucketRequest;
import com.example.taste.domain.store.dto.response.StoreBucketResponse;
import com.example.taste.domain.store.dto.response.StoreResponse;
import com.example.taste.domain.store.entity.Store;
import com.example.taste.domain.store.entity.StoreBucket;
import com.example.taste.domain.store.entity.StoreBucketItem;
import com.example.taste.domain.store.repository.StoreBucketItemRepository;
import com.example.taste.domain.store.repository.StoreBucketRepository;
import com.example.taste.domain.store.repository.StoreRepository;
import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StoreBucketServiceImpl implements StoreService {

	private final StoreBucketRepository storeBucketRepository;
	private final UserRepository userRepository;
	private final StoreRepository storeRepository;
	private final StoreBucketItemRepository storeBucketItemRepository;

	@Override
	public StoreBucketResponse createBucket(CreateBucketRequest request, Long userId) {
		User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(USER_NOT_FOUND));
		StoreBucket storeBucket = StoreBucket.builder()
			.user(user)
			.name(request.getName())
			.isOpened(request.getIsOpened())
			.build();
		return StoreBucketResponse.from(storeBucketRepository.save(storeBucket));
	}

	@Override
	public void addBucketItem(AddBucketItemRequest request) {
		Store store = storeRepository.findById(request.getStoreId())
			.orElseThrow(() -> new CustomException(STORE_NOT_FOUND));

		for (Long bucketId : request.getBucketIds()) {
			StoreBucket storeBucket = storeBucketRepository.findById(bucketId)
				.orElseThrow(() -> new CustomException(BUCKET_NOT_FOUND));

			StoreBucketItem storeBucketItem = StoreBucketItem.builder()
				.storeBucket(storeBucket)
				.store(store)
				.build();

			storeBucketItemRepository.save(storeBucketItem);
		}
	}

	@Override
	public StoreResponse getStore(Long id) {
		Store store = storeRepository.findById(id).orElseThrow(() -> new CustomException(STORE_NOT_FOUND));
		return StoreResponse.from(store);
	}
}
