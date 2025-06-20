package com.example.taste.domain.event.batch;

import java.time.LocalDate;
import java.util.List;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import com.example.taste.common.batch.RetryUtils;
import com.example.taste.domain.event.entity.Event;
import com.example.taste.domain.event.service.EventService;
import com.example.taste.domain.pk.enums.PkType;
import com.example.taste.domain.pk.service.PkService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class EventWinnerBatchConfig extends DefaultBatchConfiguration {

	private final EventService eventService;
	private final PkService pkService;
	public static final int RETRY_LIMIT = 3;

	@Bean
	public Job eventWinnerJob(JobRepository repo, Step winnerStep) {
		return new JobBuilder("EventWinnerJob", repo)
			.start(winnerStep)
			.build();
	}

	@Bean
	public Step winnerStep(JobRepository repo, PlatformTransactionManager transactionManager) {
		return new StepBuilder("EventWinnerStep", repo)
			.<Event, Event>chunk(500, transactionManager)
			.reader(eventReader())
			.writer(eventWriter())
			.build();
	}

	@Bean
	@StepScope
	public ItemReader<Event> eventReader() {
		LocalDate yesterday = LocalDate.now().minusDays(1);

		List<Event> events = RetryUtils.executeWithRetry(
			() -> eventService.findEndedEventList(yesterday),
			RETRY_LIMIT,
			"Reader - [Event] 종료된 이벤트 목록 조회");

		log.info("[Event] 종료된 이벤트 수: {}", events.size());
		return new ListItemReader<>(events);
	}

	@Bean
	@StepScope
	public ItemWriter<Event> eventWriter() {
		return events -> {
			for (Event event : events) {
				RetryUtils.executeWithRetry(() -> {
					eventService.findWinningBoard(event.getId())
						.ifPresent(winner -> {
							Long userId = winner.getUser().getId();
							pkService.savePkLog(userId, PkType.EVENT);
							log.info("Writer - [Event] 이벤트 ID: {}, 우승자 ID: {}", event.getId(), userId);
						});
					return null;
				}, RETRY_LIMIT, "Writer - [Event] eventId: " + event.getId());
			}
		};
	}

}
