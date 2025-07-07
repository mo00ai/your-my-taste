package com.example.taste.domain.board.service;

import static com.example.taste.common.constant.RedisConst.*;
import static com.example.taste.domain.board.entity.AccessPolicy.*;
import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.taste.common.service.RedisService;
import com.example.taste.domain.board.entity.Board;
import com.example.taste.domain.board.repository.BoardRepository;
import com.example.taste.domain.board.repository.FcfsInformationRepository;
import com.example.taste.domain.image.entity.Image;
import com.example.taste.domain.store.entity.Category;
import com.example.taste.domain.store.entity.Store;
import com.example.taste.domain.store.repository.CategoryRepository;
import com.example.taste.domain.store.repository.StoreRepository;
import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.repository.UserRepository;
import com.example.taste.fixtures.BoardFixture;
import com.example.taste.fixtures.CategoryFixture;
import com.example.taste.fixtures.ImageFixture;
import com.example.taste.fixtures.StoreFixture;
import com.example.taste.fixtures.UserFixture;
import com.example.taste.property.AbstractIntegrationTest;

@SpringBootTest
class FcfsInformationServiceTest extends AbstractIntegrationTest {

	@Autowired
	private FcfsInformationService fcfsInformationService;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private CategoryRepository categoryRepository;
	@Autowired
	private StoreRepository storeRepository;
	@Autowired
	private BoardRepository boardRepository;
	@Autowired
	private RedisService redisService;
	@Autowired
	private FcfsInformationRepository fcfsInformationRepository;

	@Test
	void saveFcfsInfoToDB_success() {
		// given
		Image image = ImageFixture.create();
		User user = userRepository.save(UserFixture.createNoMorePosting(image));
		Category category = categoryRepository.save(CategoryFixture.create());
		Store store = storeRepository.save(StoreFixture.create(category));
		Board board = boardRepository.save(
			BoardFixture.createOBoard("title", "contents", "O", TIMEATTACK.name(), 10,
				LocalDateTime.now().plusDays(1), store, user));

		String key = FCFS_KEY_PREFIX + board.getId();
		redisService.addToZSet(key, user.getId(), System.currentTimeMillis());

		// when
		fcfsInformationService.saveFcfsInfoToDB(key, board.getId());

		// then
		assertThat(fcfsInformationRepository.existsByBoardIdAndUserId(board.getId(), user.getId())).isTrue();

		// clean-up
		redisService.deleteKey(key);
		boardRepository.deleteById(board.getId());
		userRepository.deleteById(user.getId());
		storeRepository.deleteById(store.getId());
		categoryRepository.deleteById(category.getId());
	}
}