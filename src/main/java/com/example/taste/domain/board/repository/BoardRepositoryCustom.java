package com.example.taste.domain.board.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.taste.domain.board.dto.response.BoardListResponseDto;
import com.example.taste.domain.board.dto.search.BoardSearchCondition;
import com.example.taste.domain.board.entity.Board;

public interface BoardRepositoryCustom {

	List<Board> searchBoardDetailList(List<Long> userIdList, String type, String status, Pageable pageable);

	// 키워드 기반 검색
	Page<BoardListResponseDto> searchBoardsByKeyword(BoardSearchCondition condition, Pageable pageable);

}
