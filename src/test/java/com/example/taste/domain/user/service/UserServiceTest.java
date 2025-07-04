package com.example.taste.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Transactional;

import com.example.taste.domain.favor.repository.FavorRepository;
import com.example.taste.domain.pk.repository.PkLogRepository;
import com.example.taste.domain.store.repository.StoreBucketRepository;
import com.example.taste.domain.user.dto.request.UserFavorUpdateRequestDto;
import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.entity.UserFavor;
import com.example.taste.domain.user.repository.UserFavorRepository;
import com.example.taste.domain.user.repository.UserRepository;
import com.example.taste.fixtures.FavorFixture;
import com.example.taste.fixtures.UserFixture;
import com.example.taste.property.AbstractIntegrationTest;

@Transactional
@SpringBootTest(classes = {UserService.class, org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder.class})
public class UserServiceTest extends AbstractIntegrationTest {
	@Autowired
	private UserService userService;
	@MockitoBean
	private UserRepository userRepository;
	@MockitoSpyBean
	private FavorRepository favorRepository;
	@MockitoSpyBean
	private UserFavorRepository userFavorRepository;
	@MockitoBean
	private StoreBucketRepository storeBucketRepository;
	@MockitoBean
	private PkLogRepository pkLogRepository;

	@Test
	@DisplayName("관리자가 지정한 입맛이 아니면 저장되지 않는다")
	public void updateUserFavor() {
		// given
		Long userId = 1L;
		User user = UserFixture.create(null);
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(favorRepository.findAll()).thenReturn(FavorFixture.createFavorList());

		List<String> invalidUserFavorList = List.of("눅눅 감튀", "빠삭 감튀");
		List<UserFavorUpdateRequestDto> requestDtoList =
			invalidUserFavorList.stream()
				.map(f -> new UserFavorUpdateRequestDto(null, f))
				.toList();

		// when
		userService.updateUserFavors(userId, requestDtoList);

		// then
		verify(userFavorRepository).saveAll(anyList());
		List<UserFavor> savedFavors = userFavorRepository.findAll();

		assertThat(savedFavors.stream()
			.map(uf -> uf.getFavor().getName())
			.anyMatch(invalidUserFavorList::contains)).isFalse()
			.as("FavorRepo 저장된 입맛이 아니라면 UserFavorRepo 에 저장되지 않는다");
	}

}
