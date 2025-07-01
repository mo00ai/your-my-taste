package com.example.taste.fixtures;

import com.example.taste.domain.image.entity.Image;
import com.example.taste.domain.review.entity.Review;
import com.example.taste.domain.store.entity.Store;
import com.example.taste.domain.user.entity.User;

public class ReviewFixture {
	public static Review create(Image image, User user, Store store) {
		return Review.builder()
			.contents("맛있어요")
			.score(5)
			.image(image)
			.user(user)
			.store(store)
			.isValidated(true)
			.build();
	}
}
