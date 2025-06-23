// package com.example.taste.domain.pk.scheduler;
//
// import java.time.LocalDate;
//
// import org.springframework.scheduling.annotation.Scheduled;
//
// import com.example.taste.domain.pk.service.PkService;
//
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
//
// @Slf4j
// // @Component
// @RequiredArgsConstructor
// public class PkTermScheduler {
//
// 	private final PkService pkService;
//
// 	@Scheduled(cron = "0 0 0 1 * *")
// 	// @Scheduled(cron = "0 */5 * * * *")
// 	// @Scheduled(cron = "0 */2 * * * *")
// 	public void changePkTerm() {
//
// 		log.info("[PK TERM] 월간 랭킹 및 포인트 초기화 스케줄러 시작");
//
// 		try {
// 			pkService.runPkTermRankingScheduler(LocalDate.now());
//
// 			log.info("[PK TERM] 월간 랭킹 및 포인트 초기화 스케줄러 완료");
//
// 		} catch (Exception e) {
// 			log.error("[PK TERM] 랭킹 스케줄러 실행 실패", e);
// 			//Todo Slf4j 로그 생성, 재시도 로직
// 		}
// 	}
//
// }
