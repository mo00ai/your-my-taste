package com.example.taste.domain.board.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.example.taste.domain.board.entity.AccessPolicy;

public interface BoardRepositoryJooqCustom {
	List<Long> findExpiredTimeAttackBoardIds(AccessPolicy policy);

	List<Long> findExpiredTimeAttackBoardIds(AccessPolicy policy, Pageable pageable);

	List<Long> findExpiredTimeAttackBoardIds(AccessPolicy policy, Long seenId, int size);

	long closeBoardsByIds(List<? extends Long> ids);
}
