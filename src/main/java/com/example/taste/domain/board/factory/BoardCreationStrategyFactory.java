package com.example.taste.domain.board.factory;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.example.taste.common.exception.CustomException;
import com.example.taste.domain.board.dto.request.BoardRequestDto;
import com.example.taste.domain.board.entity.BoardType;
import com.example.taste.domain.board.exception.BoardErrorCode;
import com.example.taste.domain.board.strategy.BoardCreationStrategy;
import com.example.taste.domain.board.strategy.NormalBoardCreationStrategy;
import com.example.taste.domain.board.strategy.OpenRunBoardCreationStrategy;

@Component
public class BoardCreationStrategyFactory {

	private final Map<BoardType, BoardCreationStrategy<? extends BoardRequestDto>> strategies = new HashMap<>();

	public BoardCreationStrategyFactory() {
		strategies.put(BoardType.N, new NormalBoardCreationStrategy());
		strategies.put(BoardType.O, new OpenRunBoardCreationStrategy());

	}

	public BoardCreationStrategy<? extends BoardRequestDto> getStrategy(BoardType boardType) {
		BoardCreationStrategy<? extends BoardRequestDto> strategy = strategies.get(boardType);
		if (strategy == null) {
			throw new CustomException(BoardErrorCode.BOARD_TYPE_NOT_FOUND);
		}
		return strategy;
	}
}
