package com.example.taste.domain.auth.service;

import static com.example.taste.domain.user.exception.UserErrorCode.CONFLICT_EMAIL;
import static com.example.taste.domain.user.exception.UserErrorCode.DEACTIVATED_USER;
import static com.example.taste.domain.user.exception.UserErrorCode.INVALID_PASSWORD;
import static com.example.taste.domain.user.exception.UserErrorCode.NOT_FOUND_USER;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.taste.common.exception.CustomException;
import com.example.taste.domain.auth.dto.SignUpRequestDto;
import com.example.taste.domain.auth.dto.SigninRequestDto;
import com.example.taste.domain.image.entity.Image;
import com.example.taste.domain.image.service.ImageService;
import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final ImageService imageService;

	// 회원 가입
	@Transactional
	public void signup(SignUpRequestDto requestDto, MultipartFile file) {
		// 유저 이메일(아이디) 중복 검사
		if (isExistsEmail(requestDto.getEmail())) {
			throw new CustomException(CONFLICT_EMAIL);
		}

		// 비밀번호 인코딩
		String encodedPwd = passwordEncoder.encode(requestDto.getPassword());
		requestDto.setPassword(encodedPwd);

		Image image = null;
		// 프로필 이미지 저장
		// if (file != null) {
		// 	try {
		// 		image = imageService.saveImage(file, ImageType.USER);    // TODO: 리턴값 받는걸로 변경 질문
		// 	} catch (IOException e) {    // 이미지 저장 실패하더라도 회원가입 진행 // TODO: 이미지 트랜잭션 확인 필요
		// 		log.info("유저 이미지 저장에 실패하였습니다 (email: " + requestDto.getEmail() + ")");
		// 	}
		// }

		// 유저 정보 저장
		User user = new User(requestDto, image);
		userRepository.save(user);
	}

	public void login(SigninRequestDto requestDto) {
		// 이메일 검증
		User user = userRepository.findUserByEmail(requestDto.getEmail()).orElseThrow(
			() -> new CustomException(NOT_FOUND_USER));

		// 탈퇴한 사용자 검증
		if (user.getDeletedAt() != null) {
			throw new CustomException(DEACTIVATED_USER);
		}

		// 비밀번호 검증
		if (!isValidPassword(requestDto.getPassword(), user.getPassword())) {
			throw new CustomException(INVALID_PASSWORD);
		}

		// 세션 저장
	}

	// 중복 이메일 검사
	private boolean isExistsEmail(String email) {
		return userRepository.existsByEmail(email);
	}

	// 비밀번호 일치 검사
	private boolean isValidPassword(String rawPassword, String encodePassword) {
		return passwordEncoder.matches(rawPassword, encodePassword);
	}
}
