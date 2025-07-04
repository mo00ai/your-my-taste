package com.example.taste.domain.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Transactional;

import com.example.taste.domain.favor.repository.FavorRepository;
import com.example.taste.domain.user.repository.UserFavorRepository;
import com.example.taste.domain.user.repository.UserRepository;
import com.example.taste.property.AbstractIntegrationTest;

@Transactional
@SpringBootTest
public class UserServiceTest extends AbstractIntegrationTest {
	@Autowired
	private UserService userService;
	@MockitoBean
	private UserRepository userRepository;
	@MockitoSpyBean
	private FavorRepository favorRepository;
	@MockitoSpyBean
	private UserFavorRepository userFavorRepository;

	/*
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

	 */

}
