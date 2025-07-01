package com.example.taste.domain.user.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import com.example.taste.domain.auth.dto.SignupRequestDto;
import com.example.taste.domain.image.service.ImageService;
import com.example.taste.domain.user.dto.request.UserFavorUpdateRequestDto;
import com.example.taste.domain.user.dto.request.UserUpdateRequestDto;
import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.enums.Gender;
import com.example.taste.domain.user.enums.Role;
import com.example.taste.domain.user.repository.UserRepository;
import com.example.taste.domain.user.service.UserInternalService;
import com.example.taste.domain.user.service.UserService;
import com.example.taste.fixtures.UserFixture;
import com.example.taste.property.AbstractIntegrationTest;

@Transactional
@SpringBootTest
public class UserFacadeTest extends AbstractIntegrationTest {            // userInternalService 까지 검증
	@Autowired
	private UserFacade userFacade;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private UserInternalService userInternalService;
	@MockitoBean
	private ImageService imageService;
	@MockitoBean
	private UserService userService;
	@MockitoBean
	private PasswordEncoder passwordEncoder;

	@Test
	@DisplayName("회원 가입 중 이미지 업로드에 실패해도 회원 가입에 성공")
	public void signupSuccessDespiteImageUploadFailure() throws Exception {
		// given
		SignupRequestDto requestDto = new SignupRequestDto(
			"email@email.com", "password", "nickname", "Seoul",
			List.of(new UserFavorUpdateRequestDto(null, "단짠단짠")), Gender.FEMALE.toString(),
			30, Role.USER.toString());
		MockMultipartFile image = new MockMultipartFile(
			"file", "test.png", "image/png", "이미지데이터".getBytes());

		// when
		doNothing().when(userService).updateUserFavors(any(), any());
		doThrow(new IOException("파일 저장 실패")).when(imageService).saveImage(any(), any());
		userFacade.signup(requestDto, image);

		// then
		Optional<User> user = userRepository.findUserByEmail(requestDto.getEmail());

		assertThat(user)
			.isPresent()
			.as("유저가 저장되어야 한다");

		assertThat(user.get().getEmail())
			.isEqualTo(requestDto.getEmail())
			.as("requestDto 의 필드(이메일)이 정상적으로 저장되어야 한다.");
	}

	@Test
	@DisplayName("회원 정보 업데이트 중 이미지 업로드에 실패해도 업데이트에 성공")
	public void updateUserSuccessDespiteImageUploadFailure() throws Exception {
		// given
		User user = userRepository.save(UserFixture.create(null));

		UserUpdateRequestDto requestDto = new UserUpdateRequestDto(
			"newNickname", "newAddress", "oldPassword", "newPassword");
		MockMultipartFile image = new MockMultipartFile(
			"file", "test.png", "image/png", "이미지데이터".getBytes());

		// when
		when(passwordEncoder.matches(any(), any())).thenReturn(true);
		doThrow(new IOException("파일 저장 실패")).when(imageService).saveImage(any(), any());
		userFacade.updateUser(user.getId(), requestDto, image);

		// then
		Optional<User> savedUser = userRepository.findById(user.getId());

		assertThat(savedUser)
			.isPresent()
			.as("유저가 저장되어야 한다");

		assertThat(savedUser.get().getNickname())
			.isEqualTo(requestDto.getNickname())
			.as("requestDto 의 필드(이메일)이 정상적으로 업데이트되어야 한다.");
	}
}
