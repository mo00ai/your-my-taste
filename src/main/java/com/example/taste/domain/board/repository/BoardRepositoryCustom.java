package com.example.taste.domain.board.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.taste.domain.board.dto.response.BoardListResponseDto;
import com.example.taste.domain.board.dto.response.OpenRunBoardResponseDto;
import com.example.taste.domain.board.dto.search.BoardSearchCondition;
import com.example.taste.domain.board.entity.AccessPolicy;
import com.example.taste.domain.board.entity.BoardType;

public interface BoardRepositoryCustom {

	// 게시글 목록 조회(내 게시글 또는 팔로우한 사람들 게시글)
	Page<BoardListResponseDto> findBoardListDtoByUserIdList(List<Long> userIds, Pageable pageable);

	// 키워드 기반 검색
	Page<BoardListResponseDto> searchBoardsByKeyword(BoardSearchCondition condition, Pageable pageable);

	//long closeBoardsByIds(List<? extends Long> ids);

	Page<OpenRunBoardResponseDto> findUndeletedBoardByTypeAndPolicy(BoardType type, Collection<AccessPolicy> statuses,
		Pageable pageable);
	// TODO 구현할 메서드
	// List<BoardListResponseDto> findByOktTrgmSearch(String searchKeywords, String originalQuery, int limit);
	//
	// List<BoardListResponseDto> findByHybridSearch(BoardSearchCondition condition);

	// long closeBoardsByIds(List<Long> ids);
}
