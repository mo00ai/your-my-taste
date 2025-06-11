package com.example.taste.domain.board.service;

import org.springframework.stereotype.Service;

import com.example.taste.common.exception.CustomException;
import com.example.taste.common.util.EntityFetcher;
import com.example.taste.domain.board.entity.Board;
import com.example.taste.domain.board.entity.Like;
import com.example.taste.domain.board.exception.BoardErrorCode;
import com.example.taste.domain.board.repository.LikeRepository;
import com.example.taste.domain.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LikeService {

	private final LikeRepository likeRepository;
	private final BoardService boardService;
	private final EntityFetcher entityFetcher;

	public void likeBoard(Long userId, Long boardId) {
		User user = entityFetcher.getUserOrThrow(userId);
		Board board = boardService.findByBoardId(boardId);
		// 이미 저장되어 있다면
		if (likeRepository.existsByUserAndBoard(user, board)) {
			throw new CustomException(BoardErrorCode.ALREADY_LIKED);
		}
		Like like = Like.builder()
			.user(user)
			.board(board)
			.build();
		likeRepository.save(like);
	}

	public void unlikeBoard(Long userId, Long boardId) {
		User user = entityFetcher.getUserOrThrow(userId);
		Board board = boardService.findByBoardId(boardId);

		Like like = likeRepository.findByUserAndBoard(user, board)
			.orElseThrow(() -> new CustomException(BoardErrorCode.LIKE_NOT_FOUND));
		likeRepository.delete(like);

	}

	public boolean isLiked(Long userId, Long boardId) {
		return likeRepository.existsByUserIdAndBoardId(userId, boardId);
	}

}
