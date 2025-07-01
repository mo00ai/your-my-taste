package com.example.taste.domain.user.facade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.taste.domain.auth.dto.SignupRequestDto;
import com.example.taste.domain.user.dto.request.UserUpdateRequestDto;
import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.service.UserInternalService;
import com.example.taste.domain.user.service.UserService;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserFacade {
	private final UserService userService;
	private final UserInternalService userInternalService;

	// 회원 가입
	@Transactional
	public void signup(SignupRequestDto requestDto, MultipartFile file) {
		// 유저 기본 정보 저장
		User user = userInternalService.signup(requestDto);

		// 입맛 취향 정보 저장
		userService.updateUserFavors(user.getId(), requestDto.getFavorList());

		// 프로필 이미지 저장
		userInternalService.uploadUserImage(user, file);
	}

	// 유저 정보 업데이트
	@Transactional
	public void updateUser(Long userId, UserUpdateRequestDto requestDto, MultipartFile file) {
		User user = userInternalService.updateUser(userId, requestDto);
		// 프로필 이미지 저장
		userInternalService.updateUserImage(user, file);
	}
}
