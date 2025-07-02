// package com.example.taste.domain.board.scheduler;
//
// import static com.example.taste.domain.board.entity.AccessPolicy.*;
//
// import java.util.List;
//
// import org.springframework.scheduling.annotation.Scheduled;
// import org.springframework.stereotype.Component;
//
// import com.example.taste.domain.board.repository.BoardRepository;
//
// import io.micrometer.core.instrument.Counter;
// import io.micrometer.core.instrument.MeterRegistry;
// import jakarta.annotation.PostConstruct;
// import jakarta.transaction.Transactional;
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
//
// @Slf4j
// @Component
// @RequiredArgsConstructor
// public class BoardScheduler {
//
// 	private final BoardRepository boardRepository;
// 	private final MeterRegistry meterRegistry;
// 	private Counter counter;
//
// 	@PostConstruct
// 	public void init() {
// 		counter = Counter.builder("counter")
// 			.description("스케줄러의 총 처리 건 수")
// 			.register(meterRegistry);
// 	}
//
// 	@Transactional
// 	@Scheduled(cron = "0 */10 * * * *")
// 	public void closeOpenRunPost() {
// 		// 1차 캐시가 bulk 연산을 덮어쓰지 않도록 board가 아닌 id 값만 반환
// 		log.info("스케줄러 실행 시간 : {}", System.currentTimeMillis());
// 		List<Long> expiredBoardIds = boardRepository.findExpiredTimeAttackBoardIds(TIMEATTACK);
//
// 		if (expiredBoardIds.isEmpty()) {
// 			log.debug("[BoardScheduler] 닫을 게시글 없음");
// 			return;
// 		}
//
// 		long updatedCount = boardRepository.closeBoardsByIds(expiredBoardIds);
// 		log.info("스케줄러 종료 시간 : {}", System.currentTimeMillis());
// 		counter.increment(updatedCount);
// 		//log.info("[BoardScheduler] 다음 게시글들을 CLOSED로 변경함: {}", expiredBoardIds);
// 		//log.info("[BoardScheduler] 총 {}건 게시글 상태 변경 완료", updatedCount);
// 	}
// }
