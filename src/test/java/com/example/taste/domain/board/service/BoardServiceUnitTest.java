package com.example.taste.domain.board.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.taste.common.exception.CustomException;
import com.example.taste.common.service.RedisService;
import com.example.taste.common.util.EntityFetcher;
import com.example.taste.domain.board.dto.request.OpenRunBoardRequestDto;
import com.example.taste.domain.board.entity.Board;
import com.example.taste.domain.board.repository.BoardRepository;
import com.example.taste.domain.image.entity.Image;
import com.example.taste.domain.image.service.BoardImageService;
import com.example.taste.domain.pk.service.PkService;
import com.example.taste.domain.store.entity.Category;
import com.example.taste.domain.store.entity.Store;
import com.example.taste.domain.store.service.StoreService;
import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.repository.UserRepository;
import com.example.taste.fixtures.BoardFixture;
import com.example.taste.fixtures.CategoryFixture;
import com.example.taste.fixtures.ImageFixture;
import com.example.taste.fixtures.StoreFixture;
import com.example.taste.fixtures.UserFixture;

//@ActiveProfiles("test-int")
@ExtendWith(MockitoExtension.class)
public class BoardServiceUnitTest {
	@InjectMocks
	private BoardService boardService;
	@Mock
	private BoardRepository boardRepository;
	@Mock
	private BoardImageService boardImageService;
	@Mock
	private StoreService storeService;
	@Mock
	private PkService pkService;
	@Mock
	private HashtagService hashtagService;
	@Mock
	private EntityFetcher entityFetcher;
	@Mock
	private RedisService redisService;
	@Mock
	private SimpMessagingTemplate messagingTemplate;
	@Mock
	private UserRepository userRepository;

	@Test
	public void findBoard_whenNotOpenYet_ThrowException() {
		// given
		Image image = ImageFixture.create();
		User user = UserFixture.create(image);
		Category category = CategoryFixture.create();
		Store store = StoreFixture.create(category);

		OpenRunBoardRequestDto dto = new OpenRunBoardRequestDto();
		ReflectionTestUtils.setField(dto, "title", "제목입니다");
		ReflectionTestUtils.setField(dto, "contents", "내용입니다");
		ReflectionTestUtils.setField(dto, "type", "O");
		ReflectionTestUtils.setField(dto, "accessPolicy", "TIMEATTACK");
		ReflectionTestUtils.setField(dto, "openLimit", 10);
		ReflectionTestUtils.setField(dto, "openTime", LocalDateTime.now().plusDays(1));

		Board board = BoardFixture.createTimeAttackBoard(dto, store, user);
		long boardId = 999L, userId = 999L;

		// stub
		given(boardRepository.findActiveBoard(anyLong())).willReturn(Optional.of(board));
		given(userRepository.findById(userId)).willReturn(Optional.of(user));

		// when, then
		assertThrows(CustomException.class, () -> {
			boardService.findBoard(boardId, userId);
		});
	}

	@Test
	public void findBoard_whenClosedBoard_ThrowException() {
		// given
		Image image = ImageFixture.create();
		User user = UserFixture.create(image);
		Category category = CategoryFixture.create();
		Store store = StoreFixture.create(category);

		OpenRunBoardRequestDto dto = new OpenRunBoardRequestDto();
		ReflectionTestUtils.setField(dto, "title", "제목입니다");
		ReflectionTestUtils.setField(dto, "contents", "내용입니다");
		ReflectionTestUtils.setField(dto, "type", "O");
		ReflectionTestUtils.setField(dto, "accessPolicy", "CLOSED");
		ReflectionTestUtils.setField(dto, "openLimit", 10);
		ReflectionTestUtils.setField(dto, "openTime", LocalDateTime.now().plusDays(1));
		Board board = BoardFixture.createClosedOBoard(dto, store, user);
		long boardId = 999L, userId = 999L;

		// stub
		given(boardRepository.findActiveBoard(anyLong())).willReturn(Optional.of(board));
		given(userRepository.findById(userId)).willReturn(Optional.of(user));

		// when, then
		assertThrows(CustomException.class, () -> {
			boardService.findBoard(boardId, userId);
		});
	}

	@Test
	public void findBoard_whenTimeExceededBoard_ThrowException() {
		// given
		Image image = ImageFixture.create();
		User user = UserFixture.create(image);
		Category category = CategoryFixture.create();
		Store store = StoreFixture.create(category);

		OpenRunBoardRequestDto dto = new OpenRunBoardRequestDto();
		ReflectionTestUtils.setField(dto, "title", "제목입니다");
		ReflectionTestUtils.setField(dto, "contents", "내용입니다");
		ReflectionTestUtils.setField(dto, "type", "O");
		ReflectionTestUtils.setField(dto, "accessPolicy", "TIMEATTACK");
		ReflectionTestUtils.setField(dto, "openLimit", 10);
		ReflectionTestUtils.setField(dto, "openTime", LocalDateTime.now().minusDays(1));
		Board board = BoardFixture.createTimeLimitedOBoard(dto, store, user);
		long boardId = 999L, userId = 999L;

		// stub
		given(boardRepository.findActiveBoard(anyLong())).willReturn(Optional.of(board));
		given(userRepository.findById(userId)).willReturn(Optional.of(user));

		// when, then
		assertThrows(CustomException.class, () -> {
			boardService.findBoard(boardId, userId);
		});
	}
}
