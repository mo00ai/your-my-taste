package com.example.taste.domain.user.facade;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import com.example.taste.domain.auth.dto.SignupRequestDto;
import com.example.taste.domain.user.dto.request.UserFavorUpdateRequestDto;
import com.example.taste.domain.user.dto.request.UserUpdateRequestDto;
import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.enums.Gender;
import com.example.taste.domain.user.enums.Role;
import com.example.taste.domain.user.service.UserInternalService;
import com.example.taste.domain.user.service.UserService;
import com.example.taste.fixtures.UserFixture;

@ExtendWith(MockitoExtension.class)
public class UserFacadeUnitTest {
	@InjectMocks
	private UserFacade userFacade;
	@Mock
	private UserInternalService userInternalService;
	@Mock
	private UserService userService;

	@Test
	@DisplayName("회원가입 정상 흐름")
	void signup() {
		// given
		SignupRequestDto requestDto = new SignupRequestDto(
			"email@email.com", "password", "nickname", "Seoul",
			List.of(new UserFavorUpdateRequestDto(null, "단짠단짠")), Gender.FEMALE.toString(),
			30, Role.USER.toString());

		User user = new User(requestDto);
		ReflectionTestUtils.setField(user, "id", 1L);
		MultipartFile file = null;

		when(userInternalService.signup(requestDto)).thenReturn(user);
		doNothing().when(userService).updateUserFavors(anyLong(), anyList());
		doNothing().when(userInternalService).uploadUserImage(user, file);

		// when
		userFacade.signup(requestDto, file);

		// then
		verify(userInternalService).signup(requestDto);
		verify(userService).updateUserFavors(user.getId(), requestDto.getFavorList());
		verify(userInternalService).uploadUserImage(user, file);
	}

	@Test
	@DisplayName("유저 정보 정상 업데이트")
	public void updateUser() {
		// given
		User user = UserFixture.create(null);
		UserUpdateRequestDto requestDto = new UserUpdateRequestDto(
			"newNickname", "newAddress",
			user.getPassword(), "newPassword");
		MultipartFile file = null;

		when(userInternalService.updateUser(user.getId(), requestDto)).thenReturn(user);
		doNothing().when(userInternalService).updateUserImage(user, file);

		// when
		userFacade.updateUser(user.getId(), requestDto, file);

		// then
		verify(userInternalService).updateUserImage(user, file);
	}
}
