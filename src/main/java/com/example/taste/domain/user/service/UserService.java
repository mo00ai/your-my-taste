package com.example.taste.domain.user.service;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import com.example.taste.domain.user.dto.UserResponseDto;
import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.entity.UserFavor;
import com.example.taste.domain.user.repository.UserFavorRepository;
import com.example.taste.domain.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {
	private final UserRepository userRepository;
	private final UserFavorRepository userFavorRepository;

	// 내 정보 조회
	public UserResponseDto getUsers(Long userId) {
		User user = userRepository.findById(userId).orElseThrow();
		List<UserFavor> favorList = userFavorRepository.findAllByUser(userId);
		return new UserResponseDto(user, favorList);
	}
}
