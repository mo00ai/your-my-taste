package com.example.taste.domain.board.strategy;

import com.example.taste.domain.board.dto.request.BoardRequestDto;
import com.example.taste.domain.board.entity.Board;
import com.example.taste.domain.store.entity.Store;
import com.example.taste.domain.user.entity.User;

public interface BoardCreationStrategy<T extends BoardRequestDto> {
	Board createBoard(T dto, Store store, User user);
}
