package com.example.taste.domain.board.batch;

import static com.example.taste.domain.board.entity.AccessPolicy.*;

import java.util.List;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.IteratorItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.transaction.PlatformTransactionManager;

import com.example.taste.domain.board.service.BoardService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class ClosingBoardBatchConfig extends DefaultBatchConfiguration {
	@Bean
	public Job closingBoardJob(JobRepository repo, Step closingBoardStep) {
		return new JobBuilder("closingBoardJob", repo)
			.start(closingBoardStep)
			.build();
	}

	@Bean
	@JobScope
	public Step closingBoardStep(JobRepository repo, PlatformTransactionManager tm, BoardService boardService) {
		return new StepBuilder("closingBoardStep", repo)
			.<Long, Long>chunk(100, tm)
			.reader(closingBoardReader(boardService))
			.writer(closingBoardWriter(boardService))
			.faultTolerant()
			.retry(DataAccessException.class)
			.retryLimit(3)
			.listener(new RetryListener() {
						  @Override
						  public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback,
							  Throwable throwable) {
							  int retryCount = context.getRetryCount();
							  int retryLimit = 3;
							  log.info("[BoardScheduler] 만료된 오픈런 게시글 close 실패. 시도 횟수: {}/3", retryCount);

							  if (retryCount >= retryLimit) {
								  log.error("[BoardScheduler] 만료된 오픈런 게시글 close 실패 - 재시도 횟수 초과", throwable);
							  }
						  }
					  }
			)
			.skip(EmptyResultDataAccessException.class) // 조회한 id가 존재하지 않을 때 (writer 호출 전 삭제됨)
			.build();
	}

	@Bean
	@StepScope
	public ItemReader<Long> closingBoardReader(BoardService boardService) {
		// 1차 캐시가 bulk 연산을 덮어쓰지 않도록 board가 아닌 id 값만 반환
		List<Long> expiredBoardIds = boardService.findExpiredTimeAttackBoardIds(TIMEATTACK);
		log.debug("[BoardScheduler] 만료된 오픈런 게시글 id: {}", expiredBoardIds);
		return new IteratorItemReader<>(expiredBoardIds); // TODO PagingItemReader, CursorItemReader 성능 비교 @김채진
	}

	@Bean
	@StepScope
	public ItemWriter<Long> closingBoardWriter(BoardService boardService) {
		return items -> {
			if (items.isEmpty()) {
				log.info("[BoardScheduler] 닫을 게시글 없음");
				return;
			}

			long updatedCount = boardService.closeBoardsByIds(items.getItems());
			log.info("[BoardScheduler] 다음 게시글들을 CLOSED로 변경함: {}", items.getItems());
			log.info("[BoardScheduler] 총 {}건 게시글 상태 변경 완료", updatedCount);
		};
	}
}