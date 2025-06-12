package com.example.taste.domain.store.service;

import static com.example.taste.common.exception.ErrorCode.*;
import static com.example.taste.domain.store.exception.StoreErrorCode.*;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.taste.common.exception.CustomException;
import com.example.taste.common.response.PageResponse;
import com.example.taste.common.util.EntityFetcher;
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
	private final EntityFetcher entityFetcher;
	private final StoreBucketRepository storeBucketRepository;
	private final UserRepository userRepository;
	private final StoreRepository storeRepository;
	private final StoreBucketItemRepository storeBucketItemRepository;

	public StoreBucketResponse createBucket(CreateBucketRequest request, Long userId) {
		User user = entityFetcher.getUserOrThrow(userId);

		// 버킷명 중복 확인
		String name = makeUnduplicateName(request.getName(), user);

		StoreBucket storeBucket = StoreBucket.builder()
			.user(user)
			.name(name)
			.isOpened(request.getIsOpened())
			.build();
		return StoreBucketResponse.from(storeBucketRepository.save(storeBucket));
	}

	@Transactional
	public void addBucketItem(AddBucketItemRequest request, Long userId) {
		Store store = entityFetcher.getStoreOrThrow(request.getStoreId());

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

	// 내 버킷 조회 - (키워드로 검색)
	public PageResponse<StoreBucketResponse> getMyBuckets(Long userId, String keyword, Pageable pageable) {

		User me = entityFetcher.getUserOrThrow(userId);
		Page<StoreBucket> bucketPage = storeBucketRepository.searchMyBuckets(me, keyword, pageable);
		return PageResponse.from(bucketPage.map(StoreBucketResponse::from));
	}

	// 내 팔로워 버킷 조회 - (키워드로 검색)
	public PageResponse<StoreBucketResponse> getBucketsOfMyFollowings(Long userId, String keyword, Pageable pageable) {
		List<Long> followingIds = userRepository.findFollowingIds(userId);
		Page<StoreBucket> bucketPage = storeBucketRepository.searchFollowingsBucketsWithKeyword(followingIds, keyword,
			pageable);
		return PageResponse.from(bucketPage.map(StoreBucketResponse::from));
	}

	// 남의 버킷 조회 - 공개된 것만(특정 유저의 맛집 리스트 조회(유저 프로필로 접근)
	public List<StoreBucketResponse> getBucketsByUserId(Long targetUserId) {
		User targetUser = entityFetcher.getUserOrThrow(targetUserId);

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
		User user = entityFetcher.getUserOrThrow(userId);
		StoreBucket storeBucket = storeBucketRepository.findById(bucketId)
			.orElseThrow(() -> new CustomException(BUCKET_NOT_FOUND));

		// 로그인 유저의 버킷인지 확인
		if (!Objects.equals(userId, storeBucket.getUser().getId())) {
			throw new CustomException(BUCKET_ACCESS_DENIED);
		}

		// 버킷명 중복 확인
		name = makeUnduplicateName(name, user);

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

	public String makeUnduplicateName(String newName, User user) {
		// "newName" 으로 시작하는 버킷 모두 조회
		List<StoreBucket> buckets = storeBucketRepository.findByUserAndNameStartingWith(user, newName);

		// newName이 특수문자를 포함하는 경우를 대비해서 quote로 감싸서 패턴 생성
		Pattern pattern = Pattern.compile("^" + Pattern.quote(newName) + "\\((\\d+)\\)$");
		boolean isEquals = false;
		int suffix = 0;

		for (StoreBucket bucket : buckets) {
			String name = bucket.getName();

			if (name.equals(newName)) {
				isEquals = true;
			}

			// "버킷(숫자)" 형식이면 숫자 추출
			Matcher matcher = pattern.matcher(name);
			if (matcher.matches()) {
				try {
					suffix = Math.max(suffix, Integer.parseInt(matcher.group(1)));
				} catch (NumberFormatException e) {
					throw new CustomException(BUCKET_NAME_OVERFLOW);
				}
			}
		}

		// 정확히 일치하는 이름이 없으면 입력값 그대로 반환
		if (!isEquals) {
			return newName;
		}

		// 이름 중복 시, 뒤에 suffix 붙인 값 반환
		return newName + "(" + (suffix + 1) + ")";
	}
}
