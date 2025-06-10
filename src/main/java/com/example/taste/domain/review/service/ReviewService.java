package com.example.taste.domain.review.service;

import java.io.IOException;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.taste.common.exception.CustomException;
import com.example.taste.common.service.RedisService;
import com.example.taste.common.util.EntityFetcher;
import com.example.taste.config.security.CustomUserDetails;
import com.example.taste.domain.image.entity.Image;
import com.example.taste.domain.image.enums.ImageType;
import com.example.taste.domain.image.service.ImageService;
import com.example.taste.domain.pk.enums.PkType;
import com.example.taste.domain.pk.service.PkService;
import com.example.taste.domain.review.dto.CreateReviewRequestDto;
import com.example.taste.domain.review.dto.CreateReviewResponseDto;
import com.example.taste.domain.review.dto.GetReviewResponseDto;
import com.example.taste.domain.review.dto.UpdateReviewRequestDto;
import com.example.taste.domain.review.dto.UpdateReviewResponseDto;
import com.example.taste.domain.review.entity.Review;
import com.example.taste.domain.review.exception.ReviewErrorCode;
import com.example.taste.domain.review.repository.ReviewRepository;
import com.example.taste.domain.store.entity.Store;
import com.example.taste.domain.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewService {
	private final EntityFetcher entityFetcher;
	private final ReviewRepository reviewRepository;
	private final RedisService redisService;
	private final ImageService imageService;
	private final PkService pkService;

	@Transactional
	public CreateReviewResponseDto createReview(CreateReviewRequestDto requestDto, Long storeId,
		List<MultipartFile> files, ImageType imageType, CustomUserDetails userDetails) throws IOException {
		Image image = null;
		if (files != null && !files.isEmpty()) {
			image = imageService.saveImage(files.get(0), imageType);
		}
		Store store = entityFetcher.getStoreOrThrow(storeId);
		User user = entityFetcher.getUserOrThrow(userDetails.getId());

		String key = "reviewValidation:user:" + user.getId() + ":store:" + store.getId();
		Object value = redisService.getKeyValue(key);
		Boolean valid = Boolean.parseBoolean(String.valueOf(value));

		Review review = Review.builder()
			.contents(requestDto.getContents())
			.image(image)
			.store(store)
			.score(requestDto.getScore())
			.user(user)
			.isValidated(valid)
			.build();

		Review saved = reviewRepository.save(review);
		pkService.savePkLog(user.getId(), PkType.REVIEW);
		return new CreateReviewResponseDto(saved);
	}

	@Transactional
	public UpdateReviewResponseDto updateReview(UpdateReviewRequestDto requestDto, Long reviewId,
		MultipartFile multipartFile, ImageType imageType, CustomUserDetails userDetails) throws IOException {
		Review review = entityFetcher.getReviewOrThrow(reviewId);
		User user = entityFetcher.getUserOrThrow(userDetails.getId());
		if (!review.getUser().equals(user)) {
			throw new CustomException(ReviewErrorCode.REVIEW_USER_MISMATCH);
		}

		String contents = null;
		if (requestDto.getContents() != null && !requestDto.getContents().isEmpty()) {
			contents = requestDto.getContents();
		}

		Image image = null;
		if (multipartFile != null) {
			image = imageService.saveImage(multipartFile, imageType);
		}

		Integer score = null;
		if (requestDto.getScore() != null) {
			score = requestDto.getScore();
		}

		String key = "reviewValidation:user:" + review.getUser().getId() + ":store:" + review.getStore().getId();
		Object value = redisService.getKeyValue(key);
		Boolean valid = Boolean.parseBoolean(String.valueOf(value));

		// 엔티티 메서드 안에서 null protection
		review.updateContents(contents);
		review.updateScore(score);
		review.updateImage(image);
		review.setValidation(valid);
		return new UpdateReviewResponseDto(review);
	}

	@Transactional(readOnly = true)
	public Page<GetReviewResponseDto> getAllReview(Long storeId, int index, int score) {
		Store store = entityFetcher.getStoreOrThrow(storeId);
		Pageable pageable = PageRequest.of(index - 1, 10);
		Page<Review> reviews = reviewRepository.getAllReview(store.getId(), pageable, score);
		return reviews.map(GetReviewResponseDto::new);
	}

	@Transactional(readOnly = true)
	public GetReviewResponseDto getReview(Long reviewId) {
		return new GetReviewResponseDto(entityFetcher.getReviewOrThrow(reviewId));
	}

	@Transactional
	public void deleteReview(Long reviewId, CustomUserDetails userDetails) {
		Review review = entityFetcher.getReviewOrThrow(reviewId);
		User user = entityFetcher.getUserOrThrow(userDetails.getId());
		if (!review.getUser().equals(user)) {
			throw new CustomException(ReviewErrorCode.REVIEW_USER_MISMATCH);
		}
		review.getStore().removeReview(review);
		reviewRepository.delete(review);
	}
}
