package com.example.taste.domain.board.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.taste.domain.board.entity.FcfsInformation;

@Repository
public interface FcfsInformationRepository extends JpaRepository<FcfsInformation, Long> {
	boolean existsByBoardId(Long boardId);

	boolean existsByBoardIdAndUserId(Long boardId, Long userId);

	Iterable<? extends FcfsInformation> findAllByBoardId(Long boardId);
}
