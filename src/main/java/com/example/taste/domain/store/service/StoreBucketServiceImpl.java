package com.example.taste.domain.store.service;

import static com.example.taste.domain.store.exception.StoreErrorCode.*;

import org.springframework.stereotype.Service;

import com.example.taste.common.exception.CustomException;
import com.example.taste.domain.store.dto.CreateBucketRequest;
import com.example.taste.domain.store.dto.StoreBucketResponse;
import com.example.taste.domain.store.entity.StoreBucket;
import com.example.taste.domain.store.repository.StoreBucketRepository;
import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StoreBucketServiceImpl implements StoreService{

	private final StoreBucketRepository storeBucketRepository;
	private final UserRepository userRepository;

	@Override
	public StoreBucketResponse createBucket(CreateBucketRequest request, Long userId) {
		User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(USER_NOT_FOUND));
		StoreBucket storeBucket = StoreBucket.builder()
			.user(user)
			.name(request.getName())
			.isOpened(request.getIsOpened())
			.build();
		return new StoreBucketResponse(storeBucketRepository.save(storeBucket));
	}
}
