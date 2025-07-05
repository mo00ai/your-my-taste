package com.example.taste.domain.favor.service;

import static com.example.taste.fixtures.FavorFixture.favorList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.example.taste.domain.favor.dto.request.FavorAdminRequestDto;
import com.example.taste.domain.favor.entity.Favor;
import com.example.taste.domain.favor.repository.FavorRepository;
import com.example.taste.domain.user.dto.request.UserFavorUpdateRequestDto;
import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.repository.UserFavorRepository;
import com.example.taste.domain.user.repository.UserRepository;
import com.example.taste.domain.user.service.UserService;
import com.example.taste.fixtures.UserFixture;
import com.example.taste.property.AbstractIntegrationTest;

@Transactional
@SpringBootTest
public class FavorAdminServiceTest extends AbstractIntegrationTest {
	@Autowired
	private FavorAdminService favorAdminService;
	@Autowired
	private UserService userService;
	@Autowired
	private FavorRepository favorRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private UserFavorRepository userFavorRepository;
	@Autowired
	EntityManager em;

	@Test
	@DisplayName("관리자가 입맛 업데이트 시 유저 입맛도 업데이트 된다")
	public void updateFavorWithUserFavor() {
		// given
		favorAdminService.createFavor(favorList.stream()
			.map(FavorAdminRequestDto::new).toList());
		em.flush();
		em.clear();

		User user = userRepository.save(UserFixture.create(null));
		List<UserFavorUpdateRequestDto> userFavorList = favorList.stream()
			.map(f -> new UserFavorUpdateRequestDto(null, f))
			.toList();
		userService.updateUserFavors(user.getId(), userFavorList);
		em.flush();
		em.clear();

		// when
		List<String> updateFavorList = List.of(favorList.get(0));
		List<Long> favorIdList = favorRepository.findAllByNameIn(
				updateFavorList)
			.stream()
			.map(Favor::getId).toList();
		List<String> updateFavorNameList = updateFavorList.stream()
			.map(uf -> "new" + uf)
			.toList();
		List<FavorAdminRequestDto> updateFavorDtoList = updateFavorNameList.stream()
			.map(FavorAdminRequestDto::new)
			.toList();

		for (int i = 0; i < favorIdList.size(); i++) {
			Long favorId = favorIdList.get(i);
			FavorAdminRequestDto requestDto = updateFavorDtoList.get(i);

			favorAdminService.updateFavor(favorId, requestDto);
		}

		// then
		assertThat(favorRepository.findAllByNameIn(updateFavorNameList)).isNotEmpty()
			.as("FavorRepo 에 업데이트 입맛이 존재한다");

		assertThat(userFavorRepository.findAll().stream()
			.map(uf -> uf.getFavor().getName())
			.anyMatch(updateFavorNameList::contains)).isTrue()
			.as("UserFavorRepo 에서도 입맛이 업데이트 되었다");
	}

	@Test
	@DisplayName("관리자가 입맛 삭제 시 유저 입맛도 삭제된다")
	public void deleteFavorWithUserFavor() {
		// given
		favorAdminService.createFavor(favorList.stream()
			.map(FavorAdminRequestDto::new).toList());
		em.flush();
		em.clear();

		User user = userRepository.save(UserFixture.create(null));
		List<UserFavorUpdateRequestDto> userFavorList = favorList.stream()
			.map(f -> new UserFavorUpdateRequestDto(null, f))
			.toList();
		userService.updateUserFavors(user.getId(), userFavorList);
		em.flush();
		em.clear();

		// when
		List<String> deleteFavorNameList = List.of(favorList.get(0));
		List<Long> favorIdList = favorRepository.findAllByNameIn(
				deleteFavorNameList)
			.stream()
			.map(Favor::getId).toList();

		for (Long favorId : favorIdList) {
			favorAdminService.deleteFavor(favorId);
		}

		// then
		assertThat(favorRepository.findAllByNameIn(deleteFavorNameList).isEmpty()).isTrue()
			.as("FavorRepo 에서 입맛이 삭제된다");

		assertThat(userFavorRepository.findAll().stream()
			.map(uf -> uf.getFavor().getName())
			.anyMatch(deleteFavorNameList::contains)).isFalse()
			.as("UserFavorRepo 에서도 입맛이 삭제된다");
	}
}
