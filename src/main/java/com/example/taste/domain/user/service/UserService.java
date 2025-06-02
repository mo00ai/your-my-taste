package com.example.taste.domain.user.service;

import static com.example.taste.domain.user.exception.UserErrorCode.INVALID_PASSWORD;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.taste.common.exception.CustomException;
import com.example.taste.domain.user.dto.UserDeleteRequestDto;
import com.example.taste.domain.user.dto.UserMyProfileResponseDto;
import com.example.taste.domain.user.dto.UserUpdateRequestDto;
import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.entity.UserFavor;
import com.example.taste.domain.user.repository.UserFavorRepository;
import com.example.taste.domain.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {
	private final UserRepository userRepository;
	private final UserFavorRepository userFavorRepository;
	private final PasswordEncoder passwordEncoder;

	// 내 정보 조회
	public UserMyProfileResponseDto getMyProfile(Long userId) {
		User user = userRepository.findById(userId).orElseThrow();
		List<UserFavor> favorList = userFavorRepository.findAllByUser(userId);
		return new UserMyProfileResponseDto(user, favorList);        // TODO: 팔로, 팔로잉, 포스터 카운트 합산 필요
	}

	// 다른 유저 프로필 조회
	public UserMyProfileResponseDto getProfile(Long userId) {
		User user = userRepository.findById(userId).orElseThrow();
		List<UserFavor> favorList = userFavorRepository.findAllByUser(userId);
		return new UserMyProfileResponseDto(user, favorList);    // TODO: 팔로, 팔로잉, 포스터 카운트 합산 필요
	}

	// 유저 정보 업데이트
	@Transactional
	public void updateUser(Long userId, UserUpdateRequestDto requestDto) {
		User user = userRepository.findById(userId).orElseThrow();
		if (!passwordEncoder.matches(requestDto.getOldPassword(), user.getPassword())) {
			throw new CustomException(INVALID_PASSWORD);
		}
		requestDto.setNewPassword(passwordEncoder.encode(requestDto.getNewPassword()));
		user.update(requestDto);
	}

	// 유저 탈퇴
	@Transactional
	public void deleteUser(Long userId, UserDeleteRequestDto requestDto) {
		User user = userRepository.findById(userId).orElseThrow();
		if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
			throw new CustomException(INVALID_PASSWORD);
		}
		user.softDelete();
	}

	@Transactional
	public void followUser(Long userId, Long followingUserId) {
		User user = userRepository.findById(userId).orElseThrow();
		User followingUser = userRepository.findById(followingUserId).orElseThrow();
		user.follow(user, followingUser);
	}

	@Transactional
	public void unfollowUser(Long userId, Long followingUserId) {
		User user = userRepository.findById(userId).orElseThrow();
		User followingUser = userRepository.findById(followingUserId).orElseThrow();
		user.unfollow(user, followingUser);
	}
}
