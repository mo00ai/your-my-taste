// package com.example.taste.domain.board.scheduler;
//
// import static com.example.taste.domain.board.entity.AccessPolicy.*;
//
// import java.util.List;
//
// import org.springframework.scheduling.annotation.Scheduled;
//
// import com.example.taste.domain.board.repository.BoardRepository;
//
// import jakarta.transaction.Transactional;
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
//
// @Slf4j
// //@Component
// @RequiredArgsConstructor
// public class BoardScheduler {
//
// 	private final BoardRepository boardRepository;
//
// 	@Transactional
// 	@Scheduled(cron = "0 */10 * * * *") // 성능 고려해서 10분 단위로만 오픈런 게시글 공개가 가능
// 	public void closeOpenRunPost() {
// 		// 1차 캐시가 bulk 연산을 덮어쓰지 않도록 board가 아닌 id 값만 반환
// 		List<Long> expiredBoardIds = boardRepository.findExpiredTimeAttackBoardIds(TIMEATTACK.name());
//
// 		if (expiredBoardIds.isEmpty()) {
// 			log.debug("[BoardScheduler] 닫을 게시글 없음");
// 			return;
// 		}
//
// 		long updatedCount = boardRepository.closeBoardsByIds(expiredBoardIds);
// 		log.info("[BoardScheduler] 다음 게시글들을 CLOSED로 변경함: {}", expiredBoardIds);
// 		log.info("[BoardScheduler] 총 {}건 게시글 상태 변경 완료", updatedCount);
// 	}
// }
