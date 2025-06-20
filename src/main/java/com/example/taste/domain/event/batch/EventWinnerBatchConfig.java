package com.example.taste.domain.event.batch;

import java.time.LocalDate;
import java.util.Iterator;
import java.util.List;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import com.example.taste.domain.event.batch.util.RetryUtils;
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
	private static final int RETRY_LIMIT = 3;

	@Bean
	public Job eventWinnerJob(JobRepository repo, Step winnerStep) {
		return new JobBuilder("EventWinnerJob", repo)
			.start(winnerStep)
			.build();
	}

	@Bean
	public Step winnerStep(JobRepository repo, PlatformTransactionManager tx) {
		return new StepBuilder("winnerStep", repo)
			.<Event, Event>chunk(500, tx) //chunksize = batch 처리 사이즈, tx = 트랜잭션 매니저
			.reader(eventReader())
			.writer(eventWriter())
			.build();
	}

	@Bean
	public ItemReader<Event> eventReader() {

		LocalDate yesterday = LocalDate.now().minusDays(1);

		List<Event> events = RetryUtils.executeWithRetry(
			() -> eventService.findEndedEventList(yesterday), RETRY_LIMIT, "Reader - 이벤트 목록 조회");

		Iterator<Event> iterator = events.iterator();

		return () -> RetryUtils.executeWithRetry(
			() -> iterator.hasNext() ? iterator.next() : null, RETRY_LIMIT, "Reader - 이벤트 하나씩 읽기");

	}

	@Bean
	public ItemWriter<Event> eventWriter() {
		return events -> {
			for (Event event : events) {
				RetryUtils.executeWithRetry(() -> {

					eventService.findWinningBoard(event.getId())
						.ifPresent(winner -> {

							Long userId = winner.getUser().getId();
							pkService.savePkLog(userId, PkType.EVENT);

							log.info("이벤트 ID: {}, 우승자 ID: {}", event.getId(), userId);
						});

					return null;
				}, RETRY_LIMIT, "Writer - eventId: " + event.getId());
			}
		};
	}

}
