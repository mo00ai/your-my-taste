package com.example.taste.domain.review.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.example.taste.domain.image.entity.QImage;
import com.example.taste.domain.review.entity.QReview;
import com.example.taste.domain.review.entity.Review;
import com.example.taste.domain.store.entity.Store;
import com.example.taste.domain.user.entity.QUser;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ReviewRepositoryImpl implements ReviewRepositoryCustom {
	@Override
	public Optional<Review> getReviewWithUser(Long reviewId) {
		QReview qReview = QReview.review;
		QUser qUser = QUser.user;
		return Optional.ofNullable(queryFactory.selectFrom(qReview)
			.leftJoin(qReview.user, qUser).fetchJoin()
			.where(
				qReview.id.eq(reviewId)
			).fetch().get(0));
	}

	private final JPAQueryFactory queryFactory;

	@Override
	public Page<Review> getAllReview(Store store, Pageable pageable, int score) {
		QImage qImage = QImage.image;
		QReview qReview = QReview.review;
		List<Review> reviews = queryFactory.selectFrom(qReview)
			.leftJoin(qReview.image, qImage).fetchJoin()
			.where(
				qReview.store.eq(store),
				qReview.isPresented.isTrue(),
				score == 0 ? null : qReview.score.eq(score)
			)
			.fetch();
		int here = (int)pageable.getOffset();
		int there = Math.min(here + pageable.getPageSize(), reviews.size());
		List<Review> sub = reviews.subList(here, there);
		return new PageImpl<Review>(sub, pageable, reviews.size());
	}
}
