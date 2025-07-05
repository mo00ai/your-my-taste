package com.example.taste.domain.auth.service;

import static com.example.taste.domain.user.exception.UserErrorCode.DEACTIVATED_USER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

import com.example.taste.common.exception.CustomException;
import com.example.taste.domain.auth.dto.SigninRequestDto;
import com.example.taste.domain.user.dto.UserSigninProjectionDto;
import com.example.taste.domain.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class AuthServiceUnitTest {
	@InjectMocks
	private AuthService authService;
	@Mock
	private UserRepository userRepository;

	@Test
	@DisplayName("탈퇴한 사용자는 로그인 실패")
	public void failToSignInWithSoftDeletedUser() {
		// given
		UserSigninProjectionDto deletedUser = mock(UserSigninProjectionDto.class);
		when(deletedUser.getEmail()).thenReturn("test@email.com");
		when(deletedUser.getPassword()).thenReturn("password");
		when(deletedUser.getDeletedAt()).thenReturn(LocalDateTime.now());

		SigninRequestDto requestDto = new SigninRequestDto(deletedUser.getEmail(), deletedUser.getPassword());
		MockHttpServletRequest request = new MockHttpServletRequest();

		when(userRepository.findSigninProjectionByEmail(deletedUser.getEmail()))
			.thenReturn(Optional.of(deletedUser));

		// when & then
		CustomException e = assertThrows(CustomException.class,
			() -> authService.signin(request, requestDto));
		assertEquals(DEACTIVATED_USER.getMessage(), e.getMessage());
	}
}
