package com.example.taste.domain.user.repository;

public interface UserRepositoryCustom {
	long resetPostingCnt();

	int increasePostingCount(Long userId, int limit);

	Integer findAgeByUserId(Long userId);
}
