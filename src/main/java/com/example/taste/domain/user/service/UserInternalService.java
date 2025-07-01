package com.example.taste.domain.user.service;

import static com.example.taste.domain.user.exception.UserErrorCode.CONFLICT_EMAIL;
import static com.example.taste.domain.user.exception.UserErrorCode.INVALID_PASSWORD;
import static com.example.taste.domain.user.exception.UserErrorCode.NOT_FOUND_USER;

import java.io.IOException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.taste.common.exception.CustomException;
import com.example.taste.domain.auth.dto.SignupRequestDto;
import com.example.taste.domain.image.entity.Image;
import com.example.taste.domain.image.enums.ImageType;
import com.example.taste.domain.image.service.ImageService;
import com.example.taste.domain.user.dto.request.UserUpdateRequestDto;
import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserInternalService {
	private final ImageService imageService;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@Transactional
	public User signup(SignupRequestDto requestDto) {
		// 유저 이메일(아이디) 중복 검사
		if (userRepository.existsByEmail(requestDto.getEmail())) {
			throw new CustomException(CONFLICT_EMAIL);
		}

		// 비밀번호 인코딩
		String encodedPwd = passwordEncoder.encode(requestDto.getPassword());
		requestDto.setPassword(encodedPwd);

		// 유저 정보 저장
		return userRepository.save(new User(requestDto));
	}

	// 유저 정보 업데이트
	@Transactional
	public User updateUser(Long userId, UserUpdateRequestDto requestDto) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new CustomException(NOT_FOUND_USER));
		if (!passwordEncoder.matches(requestDto.getOldPassword(), user.getPassword())) {
			throw new CustomException(INVALID_PASSWORD);
		}
		requestDto.setNewPassword(passwordEncoder.encode(requestDto.getNewPassword()));
		user.update(requestDto);
		return user;
	}

	public void uploadUserImage(User user, MultipartFile file) {
		if (file == null) {
			return;
		}

		try {
			Image image = imageService.saveImage(file, ImageType.USER);
			user.setImage(image);
			userRepository.save(user);
		} catch (IOException e) {
			log.warn("[UserInternalService] 회원가입 중 이미지 처리 실패. User ID: {}", user.getId());
		}
	}

	public void updateUserImage(User user, MultipartFile file) {
		if (file == null) {
			return;
		}

		try {
			Image oldImage = user.getImage();
			if (oldImage != null) {
				imageService.update(oldImage.getId(), ImageType.USER, file);
			} else {
				Image image = imageService.saveImage(file, ImageType.USER);
				user.setImage(image);
				userRepository.save(user);
			}
		} catch (IOException e) {
			log.warn("[UserInternalService] 회원가입 중 이미지 처리 실패. User ID: {}", user.getId());
		}
	}
}
