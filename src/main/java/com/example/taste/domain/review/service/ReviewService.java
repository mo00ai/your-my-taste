package com.example.taste.domain.review.service;

import java.io.IOException;
import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.taste.common.exception.CustomException;
import com.example.taste.common.service.RedisService;
import com.example.taste.common.util.EntityFetcher;
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
import com.example.taste.domain.store.exception.StoreErrorCode;
import com.example.taste.domain.store.repository.StoreRepository;
import com.example.taste.domain.user.entity.User;

@Service
@RequiredArgsConstructor
public class ReviewService {
	private final EntityFetcher entityFetcher;
	private final StoreRepository storeRepository;
	private final ReviewRepository reviewRepository;
	private final RedisService redisService;
	private final ImageService imageService;
	private final PkService pkService;

	@Transactional
	public CreateReviewResponseDto createReview(CreateReviewRequestDto requestDto, Long storeId,
		List<MultipartFile> files, ImageType imageType, User userFromDetails) throws IOException {
		// 이미지 요청이 왔으면 등록, 없으면 null
		Image image = null;
		if (files != null && !files.isEmpty()) {
			image = imageService.saveImage(files.get(0), imageType);
		}
		// 리뷰 등록할 가게
		Store store = storeRepository.findById(storeId)
			.orElseThrow(() -> new CustomException(StoreErrorCode.STORE_NOT_FOUND));
		// 리뷰 작성할 유저
		User user = userFromDetails;

		// 영수증 인증 결과 조회
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

	//todo null 이 내용 지우기 일 수도 있슴
	@Transactional
	public UpdateReviewResponseDto updateReview(UpdateReviewRequestDto requestDto, Long reviewId,
		List<MultipartFile> files, ImageType imageType, User userFromDetails) throws IOException {

		// 수정할 리뷰
		Review review = reviewRepository.findById(reviewId)
			.orElseThrow(() -> new CustomException(ReviewErrorCode.REVIEW_NOT_FOUND));
		// 유저 검증
		User user = userFromDetails;
		if (!review.getUser().isSameUser(user.getId())) {
			throw new CustomException(ReviewErrorCode.REVIEW_USER_MISMATCH);
		}

		// 리뷰 수정 요소들. 요청에서 내용이 오지 않으면 기존 내용 유지.
		String contents = null;
		if (requestDto.getContents() != null && !requestDto.getContents().isEmpty()) {
			contents = requestDto.getContents();
		}

		if (files != null && !files.isEmpty()) {
			if (review.getImage() != null) {
				imageService.update(review.getImage().getId(), imageType, files.get(0));
			} else {
				review.updateImage(imageService.saveImage(files.get(0), imageType));
			}
		}

		Integer score = null;
		if (requestDto.getScore() != null) {
			score = requestDto.getScore();
		}

		// 영수증 인증 결과 조회
		String key = "reviewValidation:user:" + review.getUser().getId() + ":store:" + review.getStore().getId();
		Object value = redisService.getKeyValue(key);
		Boolean valid = Boolean.parseBoolean(String.valueOf(value));

		// 엔티티 메서드 안에서 null protection
		review.updateContents(contents);
		review.updateScore(score);
		review.setValidation(valid);
		return new UpdateReviewResponseDto(review);
	}

	@Transactional(readOnly = true)
	public Page<GetReviewResponseDto> getAllReview(Long storeId, int index, int score) {
		// 가게의 모든 리뷰 조회
		Store store = storeRepository.findById(storeId)
			.orElseThrow(() -> new CustomException(StoreErrorCode.STORE_NOT_FOUND));
		// index는 기본값 1, 최소값 검증은 controller에서
		Pageable pageable = PageRequest.of(index - 1, 10);
		// score 입력값이 있으면, 해당 score의 리뷰만 조회함. 0인 경우(혹은 입력이 없는 경우) 모든 리뷰 조회
		Page<Review> reviews = reviewRepository.getAllReview(store.getId(), pageable, score);
		return reviews.map(GetReviewResponseDto::new);
	}

	@Transactional(readOnly = true)
	public GetReviewResponseDto getReview(Long reviewId) {
		return new GetReviewResponseDto(reviewRepository.findById(reviewId)
			.orElseThrow(() -> new CustomException(ReviewErrorCode.REVIEW_NOT_FOUND)));
	}

	@Transactional
	public void deleteReview(Long reviewId, User userFromDetails) {
		Review review = reviewRepository.findById(reviewId)
			.orElseThrow(() -> new CustomException(ReviewErrorCode.REVIEW_NOT_FOUND));
		// 유저 검증
		User user = userFromDetails;
		if (!review.getUser().isSameUser(user.getId())) {
			throw new CustomException(ReviewErrorCode.REVIEW_USER_MISMATCH);
		}
		reviewRepository.delete(review);
	}
}
