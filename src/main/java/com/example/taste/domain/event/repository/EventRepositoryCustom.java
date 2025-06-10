package com.example.taste.domain.event.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.example.taste.domain.board.entity.Board;
import com.example.taste.domain.event.entity.Event;

public interface EventRepositoryCustom {
	List<Event> findEndedEventList(LocalDate endDate);

	Optional<Board> findWinningBoard(Long eventId);
}
