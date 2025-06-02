package com.example.taste.domain.review.service;

import org.springframework.stereotype.Service;

import com.example.taste.domain.review.repository.ReviewRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewService {
	private final ReviewRepository reviewRepository;
}
