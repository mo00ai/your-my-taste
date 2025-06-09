package com.example.taste.domain.comment.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.taste.common.exception.CustomException;
import com.example.taste.common.util.EntityFetcher;
import com.example.taste.domain.board.entity.Board;
import com.example.taste.domain.comment.dto.CreateCommentRequestDto;
import com.example.taste.domain.comment.dto.CreateCommentResponseDto;
import com.example.taste.domain.comment.dto.GetCommentDto;
import com.example.taste.domain.comment.dto.UpdateCommentRequestDto;
import com.example.taste.domain.comment.dto.UpdateCommentResponseDto;
import com.example.taste.domain.comment.entity.Comment;
import com.example.taste.domain.comment.exception.CommentErrorCode;
import com.example.taste.domain.comment.repository.CommentRepository;
import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.exception.UserErrorCode;
import com.example.taste.domain.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class CommentService {
	private final EntityFetcher entityFetcher;
	private final CommentRepository commentRepository;
	private final UserRepository userRepository;
	//private final BoardRepository boardRepository;

	public CreateCommentResponseDto createComment(CreateCommentRequestDto requestDto, Long boardsId) {
		Board board = Board.builder().build();
		// 임시 유저, 세션에서 가져올것
		User user = userRepository.findById(1L)
			.orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
		Comment parent = requestDto.getParent() == null ? null :
			entityFetcher.getCommentOrThrow(requestDto.getParent());
		Comment root = parent == null ? null : parent.getRoot() == null ? parent : parent.getRoot();

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
	public UpdateCommentResponseDto updateComment(UpdateCommentRequestDto requestDto, Long commentId) {
		Comment comment = entityFetcher.getCommentOrThrow(commentId);
		comment.updateContents(requestDto.getContents());
		return new UpdateCommentResponseDto(comment);
	}

	@Transactional
	public void deleteComment(Long commentId) {
		Comment comment = entityFetcher.getCommentOrThrow(commentId);
		comment.deleteContent(LocalDateTime.now());
	}

	public Page<GetCommentDto> getAllCommentOfBoard(Long boardsId, int index) {
		// 연관관계를 통한 N+1 문제가 발생하지 않도록, root comment와 거기에 달린 comment들을 연관관계에 의존하지 않고 가져온다.
		Board board = Board.builder().build();
		Pageable pageable = PageRequest.of(index - 1, 10);
		// board에 달린 댓글 중에서 root인 댓글들만 가져온다.(root == null 인 케이스)
		Page<Comment> comments = commentRepository.findAllRootByBoard(board, pageable);
		//모든 댓글 중에서 위의 comments들을 root로 가진 댓글들만 가져온다(n+1 회피)
		List<Comment> replies = commentRepository.findAllReplies(comments);
		// 이런 식이면 댓글을 가져오고 거기의 child를 찾아 가져오고, 그 댓글의 child를 다시 가져오고...
		// 하는 방식의 n+1을 피할 수 있다.

		// root list는 나중에 page로 전환하기 위한 용도
		// root map은 대댓글을 달기 위한 root를 찾는 용도(map이 list보다 조회하기 편하다)
		List<GetCommentDto> rootsList = new ArrayList<>();
		Map<Long, GetCommentDto> rootsMap = new HashMap<>();
		// root 댓글을 전부 dto 화 시킨다.
		// dto들을 rootList와 rootMap에 저장한다.
		for (Comment comment : comments.getContent()) {
			GetCommentDto dto = new GetCommentDto(comment);
			rootsList.add(dto);
			rootsMap.put(dto.getId(), dto);
		}
		// 대댓글을 검색하기 쉽게 넣어줄 tempMap.
		// 대댓글을 검색해야 하는 이유는 root에 달린 1차 대댓글이 아니라 대댓글에 달린 n차 대댓글인 경우 그 대댓글의 child로 달려야 하기 때문
		Map<Long, GetCommentDto> temp = new HashMap<Long, GetCommentDto>();

		// 대댓글을 전부 순회한다.
		for (Comment comment : replies) {
			// 일단 대댓글을 dto로 만들고
			GetCommentDto dto = new GetCommentDto(comment);
			// tempMap 에 넣는다.
			temp.put(dto.getId(), dto);

			// 현재 조회하는 대댓글이 부모와 root 필드가 일치하는 경우
			// 이 대댓글은 root 댓글에 달린 1차 대댓글 이므로, root의 child로 등록된다.
			if (comment.getParent().getId().equals(comment.getRoot().getId())) {
				rootsMap.get(comment.getParent().getId()).getChildren().add(dto);
			} else {
				// 아닌 경우 n차 대댓글이므로, 대댓글 map에서 부모 대댓글을 찾아 child로 등록한다.
				temp.get(comment.getParent().getId()).getChildren().add(dto);
			}
		}

		// page를 만들어서 반환한다. 이 때 page의 메타데이터를 기존의 comments에서 가져온다.
		return new PageImpl<>(rootsList, comments.getPageable(), comments.getTotalElements());
	}

	public GetCommentDto getComment(Long commentId) {
		return new GetCommentDto(commentRepository.findById(commentId)
			.orElseThrow(() -> new CustomException(CommentErrorCode.COMMENT_NOT_FOUND)));
	}
}
