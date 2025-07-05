package com.example.taste.domain.user.service;

import static com.example.taste.domain.user.exception.UserErrorCode.ALREADY_FOLLOWED;
import static com.example.taste.domain.user.exception.UserErrorCode.FOLLOW_NOT_FOUND;
import static com.example.taste.domain.user.exception.UserErrorCode.INVALID_PASSWORD;
import static com.example.taste.domain.user.exception.UserErrorCode.NOT_FOUND_USER;
import static com.example.taste.fixtures.FavorFixture.favorList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.taste.common.exception.CustomException;
import com.example.taste.domain.favor.repository.FavorRepository;
import com.example.taste.domain.user.dto.request.UserDeleteRequestDto;
import com.example.taste.domain.user.dto.request.UserFavorUpdateRequestDto;
import com.example.taste.domain.user.entity.Follow;
import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.repository.FollowRepository;
import com.example.taste.domain.user.repository.UserFavorRepository;
import com.example.taste.domain.user.repository.UserRepository;
import com.example.taste.fixtures.FavorFixture;
import com.example.taste.fixtures.UserFixture;

@ExtendWith(MockitoExtension.class)
public class UserServiceUnitTest {

	@Mock
	private UserRepository userRepository;
	@Mock
	private FavorRepository favorRepository;
	@Mock
	private UserFavorRepository userFavorRepository;
	@Mock
	private FollowRepository followRepository;
	@Mock
	private PasswordEncoder passwordEncoder;

	@InjectMocks
	private UserService userService;

	@Test
	@DisplayName("존재하지 않는 유저의 프로필 조회 시 예외 발생")
	public void getDeletedUserProfile() {
		// given
		Long userId = 99999L;

		// when
		when(userRepository.findUserWithFavors(userId)).thenReturn(Optional.empty());

		// then
		CustomException e = assertThrows(CustomException.class, () -> userService.getProfile(userId));
		assertEquals(NOT_FOUND_USER.getMessage(), e.getMessage());
	}

	@Test
	@DisplayName("회원 탈퇴 시 비밀번호 불일치 시 예외 발생")
	public void deleteUserWithInvalidPassword() {
		// given
		UserDeleteRequestDto requestDto
			= new UserDeleteRequestDto("email", "invalidPwd");
		User user = UserFixture.create(null);

		when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
		when(passwordEncoder.matches(requestDto.getPassword(), user.getPassword())).thenReturn(false);

		// when & then
		CustomException e = assertThrows(CustomException.class,
			() -> userService.deleteUser(user.getId(), requestDto));
		assertEquals(INVALID_PASSWORD.getMessage(), e.getMessage());
	}

	@Test
	@DisplayName("유저 입맛 업데이트 성공")
	public void updateUserFavor() {
		// given
		User user = UserFixture.create(null);
		when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
		when(favorRepository.findAll()).thenReturn(FavorFixture.createFavorList());

		// when & then
		List<UserFavorUpdateRequestDto> requestDtoList =
			favorList.stream()
				.map(f -> new UserFavorUpdateRequestDto(null, f))
				.toList();
		userService.updateUserFavors(user.getId(), requestDtoList);
		verify(userFavorRepository).saveAll(anyList());
	}

	@Test
	@DisplayName("유저 중복 팔로우 시 예외 발생")
	public void failToDuplicateFollowing() {
		// given
		List<User> userList = UserFixture.createUsers();
		User user = userList.get(0);
		User followedUser = userList.get(1);

		user.follow(new Follow(user, followedUser));

		// when & then
		when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
		when(userRepository.findById(followedUser.getId())).thenReturn(Optional.of(followedUser));
		when(followRepository.existsByFollowerIdAndFollowingId(user.getId(), followedUser.getId()))
			.thenReturn(true);

		CustomException e = assertThrows(CustomException.class,
			() -> userService.followUser(user.getId(), followedUser.getId()));
		assertEquals(ALREADY_FOLLOWED.getMessage(), e.getMessage());
	}

	@Test
	@DisplayName("팔로우 하지 않은 유저 언팔로우 시 예외 발생")
	public void failToUnfollowing() {
		// given
		List<User> userList = UserFixture.createUsers();
		User user = userList.get(0);
		User unknownUser = userList.get(1);

		when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
		when(userRepository.findById(unknownUser.getId())).thenReturn(Optional.of(unknownUser));
		when(followRepository.findByFollowerIdAndFollowingId(user.getId(), unknownUser.getId()))
			.thenReturn(Optional.empty());

		// when & then
		CustomException e = assertThrows(CustomException.class,
			() -> userService.unfollowUser(user.getId(), unknownUser.getId()));
		assertEquals(FOLLOW_NOT_FOUND.getMessage(), e.getMessage());
	}
}
