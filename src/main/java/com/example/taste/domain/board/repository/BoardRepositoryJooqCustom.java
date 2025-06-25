package com.example.taste.domain.board.repository;

import java.util.List;

import com.example.taste.domain.board.entity.AccessPolicy;

public interface BoardRepositoryJooqCustom {
	List<Long> findExpiredTimeAttackBoardIds(AccessPolicy policy);

	long closeBoardsByIds(List<? extends Long> ids);
}
