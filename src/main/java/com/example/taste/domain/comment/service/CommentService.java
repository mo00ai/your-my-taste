package com.example.taste.domain.comment.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.taste.domain.board.entity.Board;
import com.example.taste.domain.comment.dto.CreateCommentRequestDto;
import com.example.taste.domain.comment.dto.CreateCommentResponseDto;
import com.example.taste.domain.comment.dto.DeleteCommentResponseDto;
import com.example.taste.domain.comment.dto.GetCommentDto;
import com.example.taste.domain.comment.dto.UpdateCommentRequestDto;
import com.example.taste.domain.comment.dto.UpdateCommentResponseDto;
import com.example.taste.domain.comment.entity.Comment;
import com.example.taste.domain.comment.repository.CommentRepository;
import com.example.taste.domain.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentService {
	private final CommentRepository commentRepository;
	//private final BoardRepository boardRepository;

	public CreateCommentResponseDto createComment(CreateCommentRequestDto requestDto, Long boardsId) {
		Board board = Board.builder().build();
		User user = new User(); //세션에서 가져올 것
		Comment parent = requestDto.getParent() == null ? null :
			commentRepository.findById(requestDto.getParent()).orElseThrow();
		Comment root = parent == null ? null : parent.getRoot() == null ? parent : parent.getRoot();

		Comment comment = Comment.builder()
			.content(requestDto.getContent())
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
		Comment comment = commentRepository.findById(commentId).orElseThrow();
		comment.updateContent(requestDto.getContent());
		return new UpdateCommentResponseDto(comment);
	}

	@Transactional
	public DeleteCommentResponseDto deleteComment(Long commentId) {
		Comment comment = commentRepository.findById(commentId).orElseThrow();
		comment.deleteContent(LocalDateTime.now());
		return new DeleteCommentResponseDto(comment);
	}

	public List<GetCommentDto> getAllCommentOfBoard(Long boardsId) {
		// 일단 board 에 종속된 모든 comment 를 구분 없이 가져온다.
		Board board = Board.builder().build();
		List<Comment> comments = commentRepository.findAllByBoard(board);
		List<GetCommentDto> roots = new ArrayList<>();
		Map<Long, GetCommentDto> temp = new HashMap<Long, GetCommentDto>();
		// comment들을 사용해 dto를 만들고, parent가 있는 경우 parent의 children 리스트에 넣어줌
		// dto는 일단 전부 map에 저장, parent에 넣을 필요가 있을 때 parent를 map에서 찾아서 넣음.
		// 아닌 경우 root 리스트에 들어가게 됨
		for (Comment comment : comments) {
			GetCommentDto dto = new GetCommentDto(comment);
			temp.put(dto.getId(), dto);

			// 이번에 dto로 만든 comment가 부모가 있는 경우
			if (comment.getParent() != null) {
				// 해당 코멘트의 부모의 id로 만들어진 dto를 검색한다.
				// 해당 dto의 children 리스트에 지금 만든 dto를 추가해준다.
				temp.get(comment.getParent().getId()).getChildren().add(dto);
			} else {
				// 아니면 root에 더한다
				roots.add(dto);
			}
			//결과적으로는 root만 출력하면 된다.
			//root에 저장되지 않은 dto들은 어딘가의 children으로 등록되어있기 때문에 자동으로 출력된다.
		}
		return roots;
	}

	public GetCommentDto getComment(Long commentId) {
		return new GetCommentDto(commentRepository.findById(commentId).orElseThrow());
	}
}
