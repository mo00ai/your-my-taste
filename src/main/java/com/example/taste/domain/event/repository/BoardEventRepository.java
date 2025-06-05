package com.example.taste.domain.event.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.taste.domain.board.entity.Board;
import com.example.taste.domain.event.entity.BoardEvent;
import com.example.taste.domain.event.entity.Event;

public interface BoardEventRepository extends JpaRepository<BoardEvent, Long> {
	boolean existsByEventAndBoard(Event event, Board board);

	Optional<BoardEvent> findByEventAndBoard(Event event, Board board);
}
