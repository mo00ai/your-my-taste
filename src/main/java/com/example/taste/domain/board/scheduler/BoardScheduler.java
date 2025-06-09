package com.example.taste.domain.board.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.taste.domain.board.repository.BoardRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BoardScheduler {

	private final BoardRepository boardRepository;

	@Scheduled(cron = "0 */10 * * * *") // 성능 고려해서 10분 단위로만 오픈런 게시글 공개가 가능하다고 가정
	public void closeOpenrunPost() {
		boardRepository.closeExpiredTimeAttackPosts();
	}
}
