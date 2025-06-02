package com.example.taste.domain.review.service;

import org.springframework.stereotype.Service;

import com.example.taste.domain.image.entity.Image;
import com.example.taste.domain.review.dto.CreateReviewRequestDto;
import com.example.taste.domain.review.dto.CreateReviewResponseDto;
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
			.content(requestDto.getContents())
			.image(tempImage)
			.store(tempStore)
			.score(requestDto.getScore())
			.user(tempUser)
			.validated(tempValid)
			.build();
		Review saved = reviewRepository.save(review);
		return new CreateReviewResponseDto(saved);
	}
}
