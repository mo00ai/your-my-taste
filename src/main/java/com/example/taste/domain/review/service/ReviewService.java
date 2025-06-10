package com.example.taste.domain.review.service;

import java.io.IOException;
import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.taste.common.exception.CustomException;
import com.example.taste.common.util.EntityFetcher;
import com.example.taste.common.service.RedisService;
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
		Boolean valid = value != null ? (Boolean)value : false;

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

		String contents =
			requestDto.getContents().isEmpty() ? review.getContents() :
				requestDto.getContents();
		Image image = multipartFile == null ? review.getImage() : imageService.saveImage(multipartFile, imageType);
		Integer score = requestDto.getScore() == null ? review.getScore() : requestDto.getScore();

		String key = "reviewValidation:user:" + review.getUser().getId() + ":store:" + review.getStore().getId();
		Object value = redisService.getKeyValue(key);
		Boolean valid = value != null ? (Boolean)value : false;

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
		Page<Review> reviews = reviewRepository.getAllReview(store, pageable, score);
		return reviews.map(GetReviewResponseDto::new);
	}

	@Transactional(readOnly = true)
	public GetReviewResponseDto getReview(Long reviewId) {
		return new GetReviewResponseDto(findById(reviewId));
	}

	@Transactional
	public void deleteReview(Long reviewId) {
		Review review = entityFetcher.getReviewOrThrow(reviewId);
		User user = entityFetcher.getUserOrThrow(review.getUser().getId());
		if (!review.getUser().equals(user)) {
			throw new CustomException(ReviewErrorCode.REVIEW_USER_MISMATCH);
		}

		review.getStore().removeReview(review);
	}

	@Transactional(readOnly = true)
	public Review findById(Long reviewId) {
		Review review = reviewRepository.getReviewWithUserAndStore(reviewId)
			.orElseThrow(() -> new CustomException(ReviewErrorCode.REVIEW_NOT_FOUND));
		return review;
	}
}
