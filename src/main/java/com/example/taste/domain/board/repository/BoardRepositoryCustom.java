package com.example.taste.domain.board.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.example.taste.domain.board.entity.Board;

public interface BoardRepositoryCustom {
	// sort: 정렬할 기준 필드, order: asc, desc
	List<Board> searchBoardDetailList(List<Long> userIdList, String type, String status, String sort, String order,
		Pageable pageable);
}
