package com.example.taste.domain.board.repository;

import java.util.List;

public interface BoardRepositoryJooqCustom {
	List<Long> findExpiredTimeAttackBoardIds(String policy);

	long closeBoardsByIds(List<? extends Long> ids);
}
