package com.example.taste.domain.image.repository;

import java.util.List;

import com.example.taste.domain.image.dto.ImageResponseDto;

public interface BoardImageRepositoryCustom {

	List<ImageResponseDto> findAllByBoardId(Long boardId);

}
