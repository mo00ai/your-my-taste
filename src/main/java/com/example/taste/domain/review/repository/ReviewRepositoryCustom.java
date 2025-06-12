package com.example.taste.domain.review.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.taste.domain.review.entity.Review;

public interface ReviewRepositoryCustom {
	Page<Review> getAllReview(Long storeId, Pageable pageable, int score);

	Optional<Review> getReviewWithUserAndStore(Long reviewId);
}
