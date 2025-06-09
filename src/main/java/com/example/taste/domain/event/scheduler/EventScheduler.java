package com.example.taste.domain.event.scheduler;

import java.time.LocalDate;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.taste.domain.event.entity.Event;
import com.example.taste.domain.event.service.EventService;
import com.example.taste.domain.pk.enums.PkType;
import com.example.taste.domain.pk.service.PkService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EventScheduler {

	private final EventService eventService;
	private final PkService pkService;

	@Scheduled(cron = "0 */10 * * * *")
	public void selectEventWinner() {

		LocalDate yesterday = LocalDate.now().minusDays(1);

		List<Event> eventList = eventService.findEndedEventList(yesterday);

		for (Event event : eventList) {
			eventService.findWinningBoard(event.getId())
				.ifPresent(winnerBoard -> {
					Long userId = winnerBoard.getUser().getId();
					pkService.savePkLog(userId, PkType.EVENT);
				});
		}
	}

}


