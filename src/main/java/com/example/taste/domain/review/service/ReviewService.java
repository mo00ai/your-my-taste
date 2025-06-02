package com.example.taste.domain.review.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.taste.domain.image.entity.Image;
import com.example.taste.domain.review.dto.CreateReviewRequestDto;
import com.example.taste.domain.review.dto.CreateReviewResponseDto;
import com.example.taste.domain.review.dto.GetReviewResponseDto;
import com.example.taste.domain.review.dto.UpdateReviewRequestDto;
import com.example.taste.domain.review.dto.UpdateReviewResponseDto;
import com.example.taste.domain.review.entity.Review;
import com.example.taste.domain.review.repository.ReviewRepository;
import com.example.taste.domain.store.entity.Store;
import com.example.taste.domain.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewService {
	private final ReviewRepository reviewRepository;

	public CreateReviewResponseDto createReview(CreateReviewRequestDto requestDto, Long storeId) {
		Image tempImage = Image.builder().build();
		Store tempStore = Store.builder().build();
		User tempUser = User.builder().build();
		Boolean tempValid = true;
		Review review = Review.builder()
			.contents(requestDto.getContents())
			.image(tempImage)
			.store(tempStore)
			.score(requestDto.getScore())
			.user(tempUser)
			.validated(tempValid)
			.build();
		Review saved = reviewRepository.save(review);
		return new CreateReviewResponseDto(saved);
	}

	@Transactional
	public UpdateReviewResponseDto updateReview(UpdateReviewRequestDto requestDto, Long reviewId) {
		Review review = reviewRepository.findById(reviewId).orElseThrow();
		String contents = requestDto.getContents().isEmpty() ? review.getContents() : requestDto.getContents();
		Image tempImage = requestDto.getImageID() == null ? review.getImage() : Image.builder().build();
		Integer score = requestDto.getScore() == null ? review.getScore() : requestDto.getScore();
		Boolean tempValid = true;
		review.updateContents(contents);
		review.updateScore(score);
		review.updateImage(tempImage);
		review.setValidated(tempValid);
		return new UpdateReviewResponseDto(review);
	}

	public Page<GetReviewResponseDto> getAllReview(Long storeId, int index, int score) {
		Store tempStore = Store.builder().build();
		Pageable pageable = PageRequest.of(index - 1, 10);
		Page<Review> reviews = reviewRepository.getAllReview(tempStore, pageable, score);
		return reviews.map(GetReviewResponseDto::new);
	}

	public GetReviewResponseDto getReview(Long reviewId) {
		return new GetReviewResponseDto(reviewRepository.findById(reviewId).orElseThrow());
	}
}
