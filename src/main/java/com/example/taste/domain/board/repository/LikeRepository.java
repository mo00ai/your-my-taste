package com.example.taste.domain.board.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.taste.domain.board.entity.Board;
import com.example.taste.domain.board.entity.Like;
import com.example.taste.domain.user.entity.User;

public interface LikeRepository extends JpaRepository<Like, Long> {

	Optional<Like> findByUserAndBoard(User user, Board board);

	boolean existsByUserAndBoard(User user, Board board);

	boolean existsByUserIdAndBoardId(Long userId, Long boardId);

	void deleteByUserAndBoard(User user, Board board);
}
