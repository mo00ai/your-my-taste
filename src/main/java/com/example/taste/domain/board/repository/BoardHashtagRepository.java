package com.example.taste.domain.board.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.taste.domain.board.entity.BoardHashtag;

public interface BoardHashtagRepository extends JpaRepository<BoardHashtag, Long> {
}
