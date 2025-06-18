package com.example.taste.domain.image.repository;

import java.util.List;

import com.example.taste.domain.image.dto.ImageResponseDto;
import com.example.taste.domain.image.entity.QBoardImage;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BoardImageRepositoryImpl implements BoardImageRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public List<ImageResponseDto> findAllByBoardId(Long boardId) {
		QBoardImage boardImage = QBoardImage.boardImage;

		return queryFactory
			.select(Projections.constructor(
				ImageResponseDto.class,
				boardImage.image.id,
				boardImage.image.url,
				boardImage.image.uploadFileName))
			.from(boardImage)
			.where(boardImage.board.id.eq(boardId))
			.fetch();
	}
}
