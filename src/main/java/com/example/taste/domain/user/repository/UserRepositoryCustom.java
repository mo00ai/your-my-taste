package com.example.taste.domain.user.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.example.taste.domain.user.entity.User;

public interface UserRepositoryCustom {
	long resetPostingCnt();

	// 유저 프록시 가져옴
	Page<Long> getAllUserIdPage(PageRequest pageRequest);

	Optional<User> findUserWithFavors(Long userId);
}
