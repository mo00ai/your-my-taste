package com.example.taste.domain.board.service;

import static com.example.taste.domain.board.entity.AccessPolicy.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.taste.domain.board.dto.request.NormalBoardRequestDto;
import com.example.taste.domain.board.entity.Board;
import com.example.taste.domain.board.entity.BoardHashtag;
import com.example.taste.domain.board.entity.Hashtag;
import com.example.taste.domain.board.repository.BoardHashtagRepository;
import com.example.taste.domain.board.repository.HashtagRepository;
import com.example.taste.domain.store.entity.Store;
import com.example.taste.domain.user.entity.User;
import com.example.taste.fixtures.BoardFixture;
import com.example.taste.fixtures.CategoryFixture;
import com.example.taste.fixtures.ImageFixture;
import com.example.taste.fixtures.StoreFixture;
import com.example.taste.fixtures.UserFixture;

@ExtendWith(MockitoExtension.class)
public class HashtagServiceUnitTest {

	@InjectMocks
	private HashtagService hashtagService;

	@Mock
	private HashtagRepository hashtagRepository;
	@Mock
	private BoardHashtagRepository boardHashtagRepository;

	@DisplayName("게시글에 해시태그 적용 성공")
	@Test
	public void applyHashtagsToBoard_success() {
		// given
		Long userId = 1L;

		NormalBoardRequestDto dto = new NormalBoardRequestDto();
		ReflectionTestUtils.setField(dto, "title", "김밥천국");
		ReflectionTestUtils.setField(dto, "contents", "분식 맛도리에요~");
		ReflectionTestUtils.setField(dto, "type", "N");
		ReflectionTestUtils.setField(dto, "accessPolicy", OPEN.toString()); // 예시

		User user = UserFixture.create(ImageFixture.create());
		ReflectionTestUtils.setField(user, "id", userId);
		Store store = StoreFixture.create(CategoryFixture.create());

		Board board = BoardFixture.createNormalBoard(dto, store, user);

		List<String> rawHashtags = List.of("맛집", "한식", "  한식  ", "  "); // 중복 + 공백 포함
		List<Hashtag> existing = List.of();
		List<Hashtag> savedNew = List.of(Hashtag.builder().name("한식").build());

		given(hashtagRepository.findByNameIn(anyList())).willReturn(existing);
		given(hashtagRepository.saveAll(anyList())).willReturn(savedNew);
		given(boardHashtagRepository.saveAll(anyList()))
			.willAnswer(invocation -> invocation.getArgument(0));

		// when
		hashtagService.applyHashtagsToBoard(board, rawHashtags);

		// then

		verify(hashtagRepository).findByNameIn(List.of("맛집", "한식"));
		verify(hashtagRepository).saveAll(anyList());
		verify(boardHashtagRepository).saveAll(anyList());
	}

	@DisplayName("게시글에 해시태그는 중복 해시태그 없이 하나만 저장")
	@Test
	public void applyHashtagsToBoard_duplicatesRemoved_success() {
		// given
		// given
		Long userId = 1L;

		NormalBoardRequestDto dto = new NormalBoardRequestDto();
		ReflectionTestUtils.setField(dto, "title", "김밥천국");
		ReflectionTestUtils.setField(dto, "contents", "분식 맛도리에요~");
		ReflectionTestUtils.setField(dto, "type", "N");
		ReflectionTestUtils.setField(dto, "accessPolicy", OPEN.toString()); // 예시

		User user = UserFixture.create(ImageFixture.create());
		ReflectionTestUtils.setField(user, "id", userId);
		Store store = StoreFixture.create(CategoryFixture.create());
		Board board = BoardFixture.createNormalBoard(dto, store, user);
		// 더미 해시태그 리스트(중복 포함)
		List<String> hashtags = List.of("분식", "한식", "분식", "분식");

		given(hashtagRepository.findByNameIn(anyList()))
			.willReturn(List.of(
				new Hashtag("분식"),
				new Hashtag("한식")
			));

		// when
		hashtagService.applyHashtagsToBoard(board, hashtags);

		// then
		// 중복 제거 잘 됐는지
		verify(hashtagRepository).findByNameIn(argThat(names ->
			names.contains("분식") && names.contains("한식") && names.size() == 2
		));
		// 최종 저장된 BoardHashtag가 정확히 2개인지 확인
		verify(boardHashtagRepository).saveAll(argThat((Iterable<BoardHashtag> argument) -> {
			int count = 0;
			for (BoardHashtag bh : argument) {
				count++;
			}
			return count == 2;
		}));
	}

}
