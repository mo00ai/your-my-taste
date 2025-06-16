package com.example.taste.domain.board.scheduler;

import static com.example.taste.domain.board.entity.BoardStatus.*;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.taste.domain.board.entity.Board;
import com.example.taste.domain.board.repository.BoardRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class BoardScheduler {

	private final BoardRepository boardRepository;

	@Transactional // TODO 전체 롤백 가능성 고려 @김채진
	@Scheduled(cron = "0 */10 * * * *") // 성능 고려해서 10분 단위로만 오픈런 게시글 공개가 가능하다고 가정
	public void closeOpenRunPost() {
		List<Board> expiredBoards = boardRepository.findExpiredTimeAttackBoards(TIMEATTACK.name());

		if (expiredBoards.isEmpty()) {
			log.debug("[BoardScheduler] 닫을 게시글 없음");
			return;
		}

		List<Long> expiredBoardIds = expiredBoards.stream()
			.map(Board::getId)
			.toList();

		int updatedCount = boardRepository.closeBoardsByIds(expiredBoardIds);
		log.info("[BoardScheduler] 다음 게시글들을 CLOSED로 변경함: {}", expiredBoardIds);
		log.info("[BoardScheduler] 총 {}건 게시글 상태 변경 완료", updatedCount);
	}
}
