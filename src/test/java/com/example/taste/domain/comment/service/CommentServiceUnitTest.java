package com.example.taste.domain.comment.service;

import static com.example.taste.domain.board.entity.AccessPolicy.*;
import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.taste.common.exception.CustomException;
import com.example.taste.domain.board.dto.request.OpenRunBoardRequestDto;
import com.example.taste.domain.board.entity.Board;
import com.example.taste.domain.board.repository.BoardRepository;
import com.example.taste.domain.comment.dto.CreateCommentRequestDto;
import com.example.taste.domain.comment.dto.CreateCommentResponseDto;
import com.example.taste.domain.comment.dto.GetCommentDto;
import com.example.taste.domain.comment.dto.UpdateCommentRequestDto;
import com.example.taste.domain.comment.dto.UpdateCommentResponseDto;
import com.example.taste.domain.comment.entity.Comment;
import com.example.taste.domain.comment.repository.CommentRepository;
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

@ExtendWith(MockitoExtension.class)
class CommentServiceUnitTest {
	@Mock
	private UserRepository userRepository;
	@Mock
	private CommentRepository commentRepository;
	@Mock
	private BoardRepository boardRepository;
	@Mock
	private StoreRepository storeRepository;
	@Mock
	private CategoryRepository categoryRepository;
	@InjectMocks
	private CommentService commentService;

	@Nested
	class create {
		@Test
		void createComment() {
			// given
			CreateCommentRequestDto requestDto = new CreateCommentRequestDto("test", null);
			Image image = ImageFixture.create();
			Long everyID = 1L;
			User user = UserFixture.create(image);
			ReflectionTestUtils.setField(user, "id", everyID);
			Category category = CategoryFixture.create();
			Store store = StoreFixture.create(category);
			// BoardRequestDto-OpenRunRequestDto의 상속구조로 강제 set 필요
			OpenRunBoardRequestDto dto = new OpenRunBoardRequestDto();
			ReflectionTestUtils.setField(dto, "title", "제목입니다");
			ReflectionTestUtils.setField(dto, "contents", "내용입니다");
			ReflectionTestUtils.setField(dto, "type", "O");
			ReflectionTestUtils.setField(dto, "accessPolicy", CLOSED.name());
			ReflectionTestUtils.setField(dto, "openLimit", 10);
			ReflectionTestUtils.setField(dto, "openTime", LocalDateTime.now().plusDays(1));
			Board board = BoardFixture.createClosedOBoard(dto, store, user);
			ReflectionTestUtils.setField(board, "id", everyID);
			Comment saved = Comment.builder()
				.board(board)
				.user(user)
				.contents("test")
				.build();

			given(boardRepository.findById(everyID)).willReturn(Optional.of(board));
			given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
			given(commentRepository.save(any(Comment.class))).willReturn(saved);
			// when
			CreateCommentResponseDto result = commentService.createComment(requestDto, everyID, everyID);
			// then
			assertThat(result).isNotNull();
			assertThat(result.getUserId()).isEqualTo(everyID);
			assertThat(result.getBoardId()).isEqualTo(everyID);
			assertThat(result.getContents()).isEqualTo("test");
		}

		@Test
		void createChildComment() {
			// given
			Long everyID = 1L;
			CreateCommentRequestDto requestDto = new CreateCommentRequestDto("test", everyID);
			Image image = ImageFixture.create();
			User user = UserFixture.create(image);
			ReflectionTestUtils.setField(user, "id", everyID);
			Category category = CategoryFixture.create();
			Store store = StoreFixture.create(category);
			// BoardRequestDto-OpenRunRequestDto의 상속구조로 강제 set 필요
			OpenRunBoardRequestDto dto = new OpenRunBoardRequestDto();
			ReflectionTestUtils.setField(dto, "title", "제목입니다");
			ReflectionTestUtils.setField(dto, "contents", "내용입니다");
			ReflectionTestUtils.setField(dto, "type", "O");
			ReflectionTestUtils.setField(dto, "accessPolicy", CLOSED.name());
			ReflectionTestUtils.setField(dto, "openLimit", 10);
			ReflectionTestUtils.setField(dto, "openTime", LocalDateTime.now().plusDays(1));
			Board board = BoardFixture.createClosedOBoard(dto, store, user);
			Comment parent = Comment.builder().board(board).build();
			ReflectionTestUtils.setField(board, "id", everyID);
			Comment saved = Comment.builder()
				.board(board)
				.user(user)
				.contents("test")
				.build();

			given(boardRepository.findById(everyID)).willReturn(Optional.of(board));
			given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
			given(commentRepository.save(any(Comment.class))).willReturn(saved);
			given(commentRepository.findById(everyID)).willReturn(Optional.of(parent));
			// when
			CreateCommentResponseDto result = commentService.createComment(requestDto, everyID, everyID);
			// then
			assertThat(result).isNotNull();
			assertThat(result.getUserId()).isEqualTo(everyID);
			assertThat(result.getBoardId()).isEqualTo(everyID);
			assertThat(result.getContents()).isEqualTo("test");
		}

		@Test
		void createGrandChildComment() {
			// given
			Long everyID = 1L;
			CreateCommentRequestDto requestDto = new CreateCommentRequestDto("test", everyID);
			Image image = ImageFixture.create();
			User user = UserFixture.create(image);
			ReflectionTestUtils.setField(user, "id", everyID);
			Category category = CategoryFixture.create();
			Store store = StoreFixture.create(category);
			// BoardRequestDto-OpenRunRequestDto의 상속구조로 강제 set 필요
			OpenRunBoardRequestDto dto = new OpenRunBoardRequestDto();
			ReflectionTestUtils.setField(dto, "title", "제목입니다");
			ReflectionTestUtils.setField(dto, "contents", "내용입니다");
			ReflectionTestUtils.setField(dto, "type", "O");
			ReflectionTestUtils.setField(dto, "accessPolicy", CLOSED.name());
			ReflectionTestUtils.setField(dto, "openLimit", 10);
			ReflectionTestUtils.setField(dto, "openTime", LocalDateTime.now().plusDays(1));
			Board board = BoardFixture.createClosedOBoard(dto, store, user);
			Comment grandParent = Comment.builder()
				.board(board).build();
			Comment parent = Comment.builder()
				.parent(grandParent)
				.root(grandParent)
				.board(board).build();
			ReflectionTestUtils.setField(board, "id", everyID);
			Comment saved = Comment.builder()
				.board(board)
				.user(user)
				.contents("test")
				.build();

			given(boardRepository.findById(everyID)).willReturn(Optional.of(board));
			given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
			given(commentRepository.save(any(Comment.class))).willReturn(saved);
			given(commentRepository.findById(everyID)).willReturn(Optional.of(parent));
			// when
			CreateCommentResponseDto result = commentService.createComment(requestDto, everyID, everyID);
			// then
			assertThat(result).isNotNull();
			assertThat(result.getUserId()).isEqualTo(everyID);
			assertThat(result.getBoardId()).isEqualTo(everyID);
			assertThat(result.getContents()).isEqualTo("test");
		}
	}

	@Nested
	class update {
		@Test
		void updateComment() {
			// given
			Long everyId = 1L;
			UpdateCommentRequestDto requestDto = new UpdateCommentRequestDto("update");
			Image image = ImageFixture.create();
			User user = UserFixture.create(image);
			Category category = CategoryFixture.create();
			Store store = StoreFixture.create(category);
			OpenRunBoardRequestDto dto = new OpenRunBoardRequestDto();
			ReflectionTestUtils.setField(dto, "title", "제목입니다");
			ReflectionTestUtils.setField(dto, "contents", "내용입니다");
			ReflectionTestUtils.setField(dto, "type", "O");
			ReflectionTestUtils.setField(dto, "accessPolicy", CLOSED.name());
			ReflectionTestUtils.setField(dto, "openLimit", 10);
			ReflectionTestUtils.setField(dto, "openTime", LocalDateTime.now().plusDays(1));
			Board board = BoardFixture.createClosedOBoard(dto, store, user);
			ReflectionTestUtils.setField(user, "id", everyId);
			Comment comment = Comment.builder()
				.user(user)
				.board(board)
				.contents("test")
				.build();

			given(commentRepository.findById(everyId)).willReturn(Optional.of(comment));
			given(userRepository.findById(everyId)).willReturn(Optional.of(user));
			// when
			UpdateCommentResponseDto result = commentService.updateComment(requestDto, everyId, everyId);
			// then
			assertThat(result).isNotNull();
			assertThat(result.getContents()).isEqualTo("update");
		}

		@Test
		void bad_user() {
			// given
			Long everyId = 1L;
			UpdateCommentRequestDto requestDto = new UpdateCommentRequestDto("update");
			Image image = ImageFixture.create();
			User user = UserFixture.create(image);
			ReflectionTestUtils.setField(user, "id", everyId);
			User otherUser = UserFixture.create(image);
			ReflectionTestUtils.setField(user, "id", 2L);
			Category category = CategoryFixture.create();
			Store store = StoreFixture.create(category);
			OpenRunBoardRequestDto dto = new OpenRunBoardRequestDto();
			ReflectionTestUtils.setField(dto, "title", "제목입니다");
			ReflectionTestUtils.setField(dto, "contents", "내용입니다");
			ReflectionTestUtils.setField(dto, "type", "O");
			ReflectionTestUtils.setField(dto, "accessPolicy", CLOSED.name());
			ReflectionTestUtils.setField(dto, "openLimit", 10);
			ReflectionTestUtils.setField(dto, "openTime", LocalDateTime.now().plusDays(1));
			Board board = BoardFixture.createClosedOBoard(dto, store, user);
			Comment comment = Comment.builder()
				.user(otherUser)
				.board(board)
				.contents("test")
				.build();

			given(commentRepository.findById(everyId)).willReturn(Optional.of(comment));
			given(userRepository.findById(everyId)).willReturn(Optional.of(user));
			// when & then
			assertThatThrownBy(() -> commentService.updateComment(requestDto, everyId, everyId))
				.isInstanceOf(CustomException.class)
				.hasMessageContaining("본인이 작성한 댓글이 아닙니다.");
		}
	}

	@Nested
	class delete {
		@Test
		void deleteComment() {
			// given
			Long everyId = 1L;
			UpdateCommentRequestDto requestDto = new UpdateCommentRequestDto("update");
			Image image = ImageFixture.create();
			User user = UserFixture.create(image);
			Category category = CategoryFixture.create();
			Store store = StoreFixture.create(category);
			OpenRunBoardRequestDto dto = new OpenRunBoardRequestDto();
			ReflectionTestUtils.setField(dto, "title", "제목입니다");
			ReflectionTestUtils.setField(dto, "contents", "내용입니다");
			ReflectionTestUtils.setField(dto, "type", "O");
			ReflectionTestUtils.setField(dto, "accessPolicy", CLOSED.name());
			ReflectionTestUtils.setField(dto, "openLimit", 10);
			ReflectionTestUtils.setField(dto, "openTime", LocalDateTime.now().plusDays(1));
			Board board = BoardFixture.createClosedOBoard(dto, store, user);
			ReflectionTestUtils.setField(user, "id", everyId);
			Comment comment = spy(Comment.builder()
				.user(user)
				.board(board)
				.contents("test")
				.build());
			given(commentRepository.findById(everyId)).willReturn(Optional.of(comment));
			given(userRepository.findById(everyId)).willReturn(Optional.of(user));
			// when
			commentService.deleteComment(everyId, everyId);
			// then
			then(comment).should().deleteContent(any());
		}

		@Test
		void not_your_comment_dumbass() {
			// given
			Long everyId = 1L;
			UpdateCommentRequestDto requestDto = new UpdateCommentRequestDto("update");
			Image image = ImageFixture.create();
			User user = UserFixture.create(image);
			User otherUser = UserFixture.create(image);
			ReflectionTestUtils.setField(otherUser, "id", 2L);
			Category category = CategoryFixture.create();
			Store store = StoreFixture.create(category);
			OpenRunBoardRequestDto dto = new OpenRunBoardRequestDto();
			ReflectionTestUtils.setField(dto, "title", "제목입니다");
			ReflectionTestUtils.setField(dto, "contents", "내용입니다");
			ReflectionTestUtils.setField(dto, "type", "O");
			ReflectionTestUtils.setField(dto, "accessPolicy", CLOSED.name());
			ReflectionTestUtils.setField(dto, "openLimit", 10);
			ReflectionTestUtils.setField(dto, "openTime", LocalDateTime.now().plusDays(1));
			Board board = BoardFixture.createClosedOBoard(dto, store, user);
			ReflectionTestUtils.setField(user, "id", everyId);
			Comment comment = spy(Comment.builder()
				.user(otherUser)
				.board(board)
				.contents("test")
				.build());
			given(commentRepository.findById(everyId)).willReturn(Optional.of(comment));
			given(userRepository.findById(everyId)).willReturn(Optional.of(user));
			// when & then
			assertThatThrownBy(() -> commentService.deleteComment(everyId, everyId))
				.isInstanceOf(CustomException.class)
				.hasMessageContaining("본인이 작성한 댓글이 아닙니다.");

		}
	}

	@Test
	void getAllRootCommentOfBoard() {
		Long everyId = 1L;
		UpdateCommentRequestDto requestDto = new UpdateCommentRequestDto("update");
		Image image = ImageFixture.create();
		User user = UserFixture.create(image);
		Category category = CategoryFixture.create();
		Store store = StoreFixture.create(category);
		OpenRunBoardRequestDto dto = new OpenRunBoardRequestDto();
		ReflectionTestUtils.setField(dto, "title", "제목입니다");
		ReflectionTestUtils.setField(dto, "contents", "내용입니다");
		ReflectionTestUtils.setField(dto, "type", "O");
		ReflectionTestUtils.setField(dto, "accessPolicy", CLOSED.name());
		ReflectionTestUtils.setField(dto, "openLimit", 10);
		ReflectionTestUtils.setField(dto, "openTime", LocalDateTime.now().plusDays(1));
		Board board = BoardFixture.createClosedOBoard(dto, store, user);
		ReflectionTestUtils.setField(user, "id", everyId);
		Comment comment = spy(Comment.builder()
			.user(user)
			.board(board)
			.contents("test")
			.build());
		Pageable pageable = PageRequest.of(0, 10);
		List<Comment> commentList = new ArrayList<>();
		commentList.add(comment);
		Page<Comment> commentPage = new PageImpl<>(commentList, pageable, 1);
		given(commentRepository.findAllRootByBoard(eq(everyId), any(Pageable.class))).willReturn(commentPage);

		// when
		Page<GetCommentDto> page = commentService.getAllRootCommentOfBoard(everyId, 1);

		// then
		assertThat(page).isNotNull();
		GetCommentDto resultComment = page.getContent().get(0);
		assertThat(resultComment.getContents()).isEqualTo("test");

	}

	@Test
	void getChildComment() {
		Long everyId = 1L;
		UpdateCommentRequestDto requestDto = new UpdateCommentRequestDto("update");
		Image image = ImageFixture.create();
		User user = UserFixture.create(image);
		Category category = CategoryFixture.create();
		Store store = StoreFixture.create(category);
		OpenRunBoardRequestDto dto = new OpenRunBoardRequestDto();
		ReflectionTestUtils.setField(dto, "title", "제목입니다");
		ReflectionTestUtils.setField(dto, "contents", "내용입니다");
		ReflectionTestUtils.setField(dto, "type", "O");
		ReflectionTestUtils.setField(dto, "accessPolicy", CLOSED.name());
		ReflectionTestUtils.setField(dto, "openLimit", 10);
		ReflectionTestUtils.setField(dto, "openTime", LocalDateTime.now().plusDays(1));
		Board board = BoardFixture.createClosedOBoard(dto, store, user);
		ReflectionTestUtils.setField(user, "id", everyId);
		Comment comment = spy(Comment.builder()
			.user(user)
			.board(board)
			.contents("test")
			.build());
		Pageable pageable = PageRequest.of(0, 10);
		List<Comment> commentList = new ArrayList<>();
		commentList.add(comment);
		Slice<Comment> commentPage = new PageImpl<>(commentList, pageable, 1);
		given(commentRepository.findChildComment(eq(everyId), any(Pageable.class))).willReturn(commentPage);

		// when
		Slice<GetCommentDto> page = commentService.getChildComment(everyId, 1);

		// then
		assertThat(page).isNotNull();
		GetCommentDto resultComment = page.getContent().get(0);
		assertThat(resultComment.getContents()).isEqualTo("test");

	}

	@Test
	void getComment() {
		// given
		Long everyId = 1L;
		UpdateCommentRequestDto requestDto = new UpdateCommentRequestDto("update");
		Image image = ImageFixture.create();
		User user = UserFixture.create(image);
		Category category = CategoryFixture.create();
		Store store = StoreFixture.create(category);
		OpenRunBoardRequestDto dto = new OpenRunBoardRequestDto();
		ReflectionTestUtils.setField(dto, "title", "제목입니다");
		ReflectionTestUtils.setField(dto, "contents", "내용입니다");
		ReflectionTestUtils.setField(dto, "type", "O");
		ReflectionTestUtils.setField(dto, "accessPolicy", CLOSED.name());
		ReflectionTestUtils.setField(dto, "openLimit", 10);
		ReflectionTestUtils.setField(dto, "openTime", LocalDateTime.now().plusDays(1));
		Board board = BoardFixture.createClosedOBoard(dto, store, user);
		ReflectionTestUtils.setField(user, "id", everyId);
		Comment comment = spy(Comment.builder()
			.user(user)
			.board(board)
			.contents("test")
			.build());
		given(commentRepository.findById(everyId)).willReturn(Optional.of(comment));
		// when
		GetCommentDto result = commentService.getComment(everyId);
		// then
		assertThat(result).isNotNull();
		assertThat(result.getContents()).isEqualTo("test");
	}
}
