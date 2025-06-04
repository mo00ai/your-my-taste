package com.example.taste.domain.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.taste.domain.event.entity.BoardEvent;

public interface BoardEventRepository extends JpaRepository<BoardEvent, Long> {
}
