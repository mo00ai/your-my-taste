package com.example.taste.domain.image.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.taste.domain.board.entity.Board;
import com.example.taste.domain.image.entity.BoardImage;

public interface BoardImageRepository extends JpaRepository<BoardImage, Long>, BoardImageRepositoryCustom {

	List<BoardImage> findImagesByBoard(Board board);
}
