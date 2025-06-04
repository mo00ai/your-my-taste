package com.example.taste.domain.store.service;

import static com.example.taste.common.exception.ErrorCode.*;
import static com.example.taste.domain.store.exception.StoreErrorCode.*;
import static com.example.taste.domain.user.exception.UserErrorCode.*;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.example.taste.common.exception.CustomException;
import com.example.taste.domain.store.dto.request.AddBucketItemRequest;
import com.example.taste.domain.store.dto.request.CreateBucketRequest;
import com.example.taste.domain.store.dto.request.RemoveBucketItemRequest;
import com.example.taste.domain.store.dto.response.BucketItemResponse;
import com.example.taste.domain.store.dto.response.StoreBucketResponse;
import com.example.taste.domain.store.entity.Store;
import com.example.taste.domain.store.entity.StoreBucket;
import com.example.taste.domain.store.entity.StoreBucketItem;
import com.example.taste.domain.store.repository.StoreBucketItemRepository;
import com.example.taste.domain.store.repository.StoreBucketRepository;
import com.example.taste.domain.store.repository.StoreRepository;
import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StoreBucketService {

	private final StoreBucketRepository storeBucketRepository;
	private final UserRepository userRepository;
	private final StoreRepository storeRepository;
	private final StoreBucketItemRepository storeBucketItemRepository;

	public StoreBucketResponse createBucket(CreateBucketRequest request, Long userId) {
		User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(USER_NOT_FOUND));
		List<StoreBucket> buckets = storeBucketRepository.findByUserAndNameStartingWith(user, request.getName());

		// 동일한 이름의 맛집리스트가 있는지 검증
		int suffix = 0;
		for (StoreBucket bucket : buckets) {
			String name = bucket.getName();
			// 동일한 이름의 맛집리스트가 있으면 suffix 업데이트
			if (request.getName().equals(name)) {
				suffix = Math.max(suffix, 1);
				continue;
			}

			int start = name.indexOf('(');
			int end = name.indexOf(')');
			// 접미사가 (n) 형식으로 생성된 맛집리스트가 있으면 n값 추출해서 suffix 업데이트
			if (start != -1 && end != -1 && start < end) {
				String numberStr = name.substring(start + 1, end);

				try {
					suffix = Math.max(suffix, Integer.parseInt(numberStr) + 1);
				} catch (NumberFormatException ignored) {
					// (abc)처럼 단순 문자열인 경우 exception 무시
				}
			}
		}

		// 동일한 이름의 맛집리스트가 있으면 리스트명(n) 형식으로 저장
		String name = (suffix == 0) ? request.getName() : request.getName() + "(" + suffix + ")";

		StoreBucket storeBucket = StoreBucket.builder()
			.user(user)
			.name(name)
			.isOpened(request.getIsOpened())
			.build();
		return StoreBucketResponse.from(storeBucketRepository.save(storeBucket));
	}

	@Transactional
	public void addBucketItem(AddBucketItemRequest request, Long userId) {
		Store store = storeRepository.findById(request.getStoreId())
			.orElseThrow(() -> new CustomException(STORE_NOT_FOUND));

		for (Long bucketId : request.getBucketIds()) {
			StoreBucket storeBucket = storeBucketRepository.findById(bucketId)
				.orElseThrow(() -> new CustomException(BUCKET_NOT_FOUND));

			// 로그인 유저의 버킷인지 확인
			if (!Objects.equals(storeBucket.getUser().getId(), userId)) {
				throw new CustomException(BUCKET_ACCESS_DENIED);
			}

			// 이미 버킷에 가게가 저장되어 있는지 확인
			if (storeBucketItemRepository.findByStoreAndStoreBucket(store, storeBucket).isPresent()) {
				continue;
			}

			StoreBucketItem storeBucketItem = StoreBucketItem.builder()
				.storeBucket(storeBucket)
				.store(store)
				.build();
			storeBucketItemRepository.save(storeBucketItem);
		}
	}

	public List<StoreBucketResponse> getBucketsByUserId(Long targetUserId) {
		User targetUser = userRepository.findById(targetUserId).orElseThrow(() -> new CustomException(USER_NOT_FOUND));

		// 공개된 버킷만 반환
		return storeBucketRepository.findAllByUserAndIsOpened(targetUser, true).stream()
			.map(StoreBucketResponse::from)
			.toList();
	}

	public List<BucketItemResponse> getBucketItems(Long bucketId, Long userId) {
		StoreBucket storeBucket = storeBucketRepository.findById(bucketId)
			.orElseThrow(() -> new CustomException(BUCKET_NOT_FOUND));

		// 타유저의 버킷 && 비공개 버킷이면 접근 불가
		if (!Objects.equals(storeBucket.getUser().getId(), userId) && !storeBucket.isOpened()) {
			throw new CustomException(BUCKET_ACCESS_DENIED);
		}

		return storeBucketItemRepository.findAllByStoreBucket(storeBucket).stream()
			.map(BucketItemResponse::from)
			.toList();
	}

	@Transactional
	public StoreBucketResponse updateBucketName(Long bucketId, String name, Long userId) {
		StoreBucket storeBucket = storeBucketRepository.findById(bucketId)
			.orElseThrow(() -> new CustomException(BUCKET_NOT_FOUND));

		// 로그인 유저의 버킷인지 확인
		if (!Objects.equals(userId, storeBucket.getUser().getId())) {
			throw new CustomException(BUCKET_ACCESS_DENIED);
		}

		StoreBucket updatedBucket = storeBucket.updateName(name);
		return StoreBucketResponse.from(updatedBucket);
	}

	@Transactional
	public void deleteBucket(Long bucketId, Long userId) {
		StoreBucket storeBucket = storeBucketRepository.findById(bucketId)
			.orElseThrow(() -> new CustomException(BUCKET_NOT_FOUND));

		// 로그인 유저의 버킷인지 확인
		if (!Objects.equals(userId, storeBucket.getUser().getId())) {
			throw new CustomException(BUCKET_ACCESS_DENIED);
		}

		storeBucketRepository.delete(storeBucket);
	}

	@Transactional
	public void removeBucketItem(Long bucketId, RemoveBucketItemRequest request, Long userId) {
		StoreBucket storeBucket = storeBucketRepository.findById(bucketId)
			.orElseThrow(() -> new CustomException(BUCKET_NOT_FOUND));

		// 로그인 유저의 버킷인지 확인
		if (!Objects.equals(userId, storeBucket.getUser().getId())) {
			throw new CustomException(BUCKET_ACCESS_DENIED);
		}

		List<StoreBucketItem> items = storeBucketItemRepository.findAllById(request.getBucketItemIds());

		// 일부 storeBucketItem의 id가 존재하지 않으면 error
		if (items.size() != request.getBucketItemIds().size()) {
			throw new CustomException(INVALID_INPUT_VALUE);
		}

		// bucketItem이 전달받은 bucketId 하위에 존재하는지 확인
		boolean hasInvalidItem = items.stream()
			.anyMatch(item -> !Objects.equals(item.getStoreBucket().getId(), bucketId));

		if (hasInvalidItem) {
			throw new CustomException(INVALID_INPUT_VALUE);
		}

		storeBucketItemRepository.deleteAll(items);
	}
}
