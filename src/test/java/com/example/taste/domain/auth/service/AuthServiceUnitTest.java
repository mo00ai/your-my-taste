package com.example.taste.domain.auth.service;

import static com.example.taste.domain.user.exception.UserErrorCode.DEACTIVATED_USER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.example.taste.common.exception.CustomException;
import com.example.taste.domain.auth.dto.SigninRequestDto;
import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.repository.UserRepository;
import com.example.taste.fixtures.UserFixture;

@ExtendWith(MockitoExtension.class)
public class AuthServiceUnitTest {
	@InjectMocks
	private AuthService authService;
	@MockitoBean
	private UserRepository userRepository;
	@MockitoBean
	private PasswordEncoder passwordEncoder;

	@Test
	@DisplayName("탈퇴한 사용자는 로그인 실패")
	public void failToSignInWithSoftDeletedUser() {
		// given
		User deletedUser = UserFixture.createSoftDeletedUser(null);
		SigninRequestDto requestDto = new SigninRequestDto(deletedUser.getEmail(), deletedUser.getPassword());

		// when & then
		CustomException e = assertThrows(CustomException.class,
			() -> authService.signin(any(), requestDto));
		assertEquals(DEACTIVATED_USER.getMessage(), e.getMessage());
	}
}
