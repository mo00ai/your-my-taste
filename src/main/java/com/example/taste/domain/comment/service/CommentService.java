package com.example.taste.domain.comment.service;

import static com.example.taste.domain.user.exception.UserErrorCode.NOT_FOUND_USER;

import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.taste.common.exception.CustomException;
import com.example.taste.domain.board.entity.Board;
import com.example.taste.domain.board.exception.BoardErrorCode;
import com.example.taste.domain.board.repository.BoardRepository;
import com.example.taste.domain.comment.dto.CreateCommentRequestDto;
import com.example.taste.domain.comment.dto.CreateCommentResponseDto;
import com.example.taste.domain.comment.dto.GetCommentDto;
import com.example.taste.domain.comment.dto.UpdateCommentRequestDto;
import com.example.taste.domain.comment.dto.UpdateCommentResponseDto;
import com.example.taste.domain.comment.entity.Comment;
import com.example.taste.domain.comment.exception.CommentErrorCode;
import com.example.taste.domain.comment.repository.CommentRepository;
import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class CommentService {
	private final UserRepository userRepository;
	private final CommentRepository commentRepository;
	private final BoardRepository boardRepository;

	public CreateCommentResponseDto createComment(CreateCommentRequestDto requestDto, Long boardId,
		Long userId) {
		// 댓글 달 보드
		Board board = boardRepository.findById(boardId)
			.orElseThrow(() -> new CustomException(BoardErrorCode.BOARD_NOT_FOUND));

		// 부모댓글, root 댓글 설정
		Comment parent = null;
		Comment root = null;

		// 요청에서 부모 댓글을 명시한 경우
		if (requestDto.getParent() != null) {
			// 부모 댓글을 찾아서 부여
			parent = commentRepository.findById(requestDto.getParent())
				.orElseThrow(() -> new CustomException(CommentErrorCode.COMMENT_NOT_FOUND));
			// 부모 댓글의 root 가 null 이 아닌 경우(부모 댓글이 root 가 아닌 경우) 부모 댓글의 root 를 가져와 root 로 설정
			// 아니면 부모 댓글을 root 로 설정
			root = parent.getRoot() != null ? parent.getRoot() : parent;
		}

		User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(NOT_FOUND_USER));
		Comment comment = Comment.builder()
			.contents(requestDto.getContents())
			.parent(parent)
			.root(root)
			.board(board)
			.user(user)
			.build();

		Comment saved = commentRepository.save(comment);

		return new CreateCommentResponseDto(saved);
	}

	@Transactional
	public UpdateCommentResponseDto updateComment(UpdateCommentRequestDto requestDto, Long commentId,
		Long userId) {
		// 수정할 댓글
		Comment comment = commentRepository.findById(commentId)
			.orElseThrow(() -> new CustomException(CommentErrorCode.COMMENT_NOT_FOUND));

		// 유저 검증
		User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(NOT_FOUND_USER));
		if (!comment.getUser().isSameUser(user.getId())) {
			throw new CustomException(CommentErrorCode.COMMENT_USER_MISMATCH);
		}
		// 댓글에서 수정할 내용은 contents 밖에 없음
		// contents 가 빈 문자열인 경우 아예 예외처리(dto 에서)
		comment.updateContents(requestDto.getContents());
		return new UpdateCommentResponseDto(comment);
	}

	@Transactional
	public void deleteComment(Long commentId, Long userId) {
		// 수정할 댓글
		Comment comment = commentRepository.findById(commentId)
			.orElseThrow(() -> new CustomException(CommentErrorCode.COMMENT_NOT_FOUND));

		// 유저 검증
		User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(NOT_FOUND_USER));
		if (!comment.getUser().isSameUser(user.getId())) {
			throw new CustomException(CommentErrorCode.COMMENT_USER_MISMATCH);
		}
		// comment 객체의 deleteContent 메서드 호출.
		// deletedAt 시간을 세팅, contents 를 삭제 메시지로 초기화, user 를 null 로 세팅
		comment.deleteContent(LocalDateTime.now());
	}

	// 루트 가져오기
	@Transactional(readOnly = true)
	public Page<GetCommentDto> getAllRootCommentOfBoard(Long boardId, int index) {
		Pageable pageable = PageRequest.of(index - 1, 10);
		Page<Comment> rootComments = commentRepository.findAllRootByBoard(boardId, pageable);
		Page<GetCommentDto> dtos = rootComments.map(GetCommentDto::new);
		return dtos;
	}

	// 대댓글 가져오기
	public Slice<GetCommentDto> getChildComment(Long commentId, int index) {
		Pageable pageable = PageRequest.of(index - 1, 10);
		Slice<Comment> rootComments = commentRepository.findChildComment(commentId, pageable);
		Slice<GetCommentDto> dtos = rootComments.map(GetCommentDto::new);
		return dtos;
	}

	@Transactional(readOnly = true)
	public GetCommentDto getComment(Long commentId) {
		return new GetCommentDto(commentRepository.findById(commentId)
			.orElseThrow(() -> new CustomException(CommentErrorCode.COMMENT_NOT_FOUND)));
	}
}
