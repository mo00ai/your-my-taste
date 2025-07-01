package com.example.taste.domain.user.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;

import com.example.taste.domain.user.entity.User;

public interface UserRepositoryCustom {
	long resetPostingCnt();

	int increasePostingCount(Long userId, int limit);

	// 유저 프록시 가져옴
	Page<Long> getAllUserIdPage(PageRequest pageRequest);

	Optional<User> findUserWithFavors(Long userId);
}
