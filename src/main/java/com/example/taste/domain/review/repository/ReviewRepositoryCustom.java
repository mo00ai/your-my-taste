package com.example.taste.domain.review.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.taste.domain.review.entity.Review;
import com.example.taste.domain.store.entity.Store;

public interface ReviewRepositoryCustom {
	Page<Review> getAllReview(Store store, Pageable pageable, int score);
}
