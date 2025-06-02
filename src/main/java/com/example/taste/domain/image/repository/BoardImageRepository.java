package com.example.taste.domain.image.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.taste.domain.image.dto.ImageResponseDto;
import com.example.taste.domain.image.entity.BoardImage;

public interface BoardImageRepository extends JpaRepository<BoardImage, Long> {
	
	@Query("select new com.example.taste.domain.image.dto.ImageResponseDto(bi.image.url,bi.image.uploadFileName) from BoardImage bi where bi.board.id = :boardId")
	List<ImageResponseDto> findAllById(@Param("boardId") Long boardId);

}
