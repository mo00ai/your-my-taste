package com.example.taste.domain.board.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.taste.domain.board.repository.BoardRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BoardScheduler {

	private final BoardRepository boardRepository;

	@Scheduled
	public void closePost() {

	}
}
