package com.example.taste.domain.user.repository;

import java.util.Optional;

import com.example.taste.domain.user.entity.User;

public interface UserRepositoryCustom {
	long resetPostingCnt();

	int increasePostingCount(Long userId, int limit);

	Integer findAgeByUserId(Long userId);

	Optional<User> findUserWithFavors(Long userId);
}
