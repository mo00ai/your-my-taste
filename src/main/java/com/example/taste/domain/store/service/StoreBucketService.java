package com.example.taste.domain.store.service;

import static com.example.taste.common.exception.ErrorCode.*;
import static com.example.taste.domain.store.exception.StoreErrorCode.*;
import static com.example.taste.domain.user.exception.UserErrorCode.*;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
		User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(USER_NOT_FOUND));
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

		storeBucketItemRepository.deleteAllByStoreBucket(storeBucket);
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
		// 괄호 숫자 패턴이 붙어 있는 경우, baseName 추출 -> newName이 "맛집(1)"이면 "맛집"만 추출해서 비교
		String baseName = newName.replaceFirst("\\((\\d+)\\)$", "");

		// baseName 으로 시작하는 버킷 모두 조회
		List<StoreBucket> buckets = storeBucketRepository.findByUserAndNameStartingWith(user, baseName);

		// "baseName(숫자)" 형태의 정규식 생성
		// baseName 정규식에 사용되는 특수문자를 포함하는 경우를 대비해서 quote 사용
		Pattern pattern = Pattern.compile("^" + Pattern.quote(baseName) + "\\((\\d+)\\)$");
		boolean isEquals = false;
		int suffix = 0;

		for (StoreBucket bucket : buckets) {
			String name = bucket.getName();

			if (name.equals(newName)) {
				isEquals = true;
			}

			// name이 baseName(숫자) 형태와 일치하면 true
			Matcher matcher = pattern.matcher(name);
			if (matcher.matches()) {
				// group(1)은 pattern 에서 (\\d+)에 해당하는 숫자 부분만 추출
				suffix = Math.max(suffix, Integer.parseInt(matcher.group(1)));
			}
		}

		// 정확히 일치하는 이름이 없으면 입력값 그대로 반환
		if (!isEquals) {
			return newName;
		}

		// suffix가 Integer의 최대값이면 baseName(-2147483648)가 반환됨
		if (suffix == Integer.MAX_VALUE) {
			throw new CustomException(BUCKET_NAME_OVERFLOW);
		}

		// 이름 중복 시, 뒤에 suffix 붙인 값 반환
		return baseName + "(" + (suffix + 1) + ")";
	}
}
