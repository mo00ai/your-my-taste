package com.example.taste.domain.event.scheduler;

import java.time.LocalDate;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;

import com.example.taste.domain.event.entity.Event;
import com.example.taste.domain.event.service.EventService;
import com.example.taste.domain.pk.enums.PkType;
import com.example.taste.domain.pk.service.PkService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
// @Component
@RequiredArgsConstructor
public class EventScheduler {

	private final EventService eventService;
	private final PkService pkService;

	@Scheduled(cron = "0 0 0 * * *")
	// @Scheduled(cron = "0 */2 * * * *")
	public void selectEventWinner() {

		log.info("[Event] 이벤트 우승자 선정 스케줄러 시작");

		try {

			LocalDate yesterday = LocalDate.now().minusDays(1);

			List<Event> eventList = eventService.findEndedEventList(yesterday);

			log.info("종료된 이벤트 수: {}", eventList.size());

			for (Event event : eventList) {
				eventService.findWinningBoard(event.getId())
					.ifPresent(winnerBoard -> {
						Long userId = winnerBoard.getUser().getId();
						pkService.savePkLog(userId, PkType.EVENT);

						log.info("이벤트 ID: {}, 우승자 ID: {}", event.getId(), userId);
					});
			}

			log.info("이벤트 우승자 선정 스케줄러 완료");

		} catch (Exception e) {
			log.error("이벤트 우승자 선정 중 오류 발생", e);

			//Todo 재시도 로직

		}
	}

}


