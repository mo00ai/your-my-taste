package com.example.taste.domain.pk.batch;

import java.time.LocalDate;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import com.example.taste.domain.pk.service.PkService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class PkTermRankingBatchConfig extends DefaultBatchConfiguration {

	private final PkService pkService;
	private static final int RETRY_LIMIT = 3;

	@Bean
	public Job pkTermRankingJob(JobRepository jobRepository, Step pkTermRankingStep) {
		return new JobBuilder("pkTermRankingJob", jobRepository)
			.start(pkTermRankingStep)
			.build();
	}

	@Bean
	public Step pkTermRankingStep(JobRepository jobRepository,
		PlatformTransactionManager transactionManager,
		Tasklet pkTermRankingTasklet) {

		return new StepBuilder("pkTermRankingStep", jobRepository)
			.tasklet(pkTermRankingTasklet, transactionManager)
			.build();
	}

	@Bean
	@StepScope
	public Tasklet pkTermRankingTasklet() {
		return (contribution, chunkContext) -> {
			RetryTemplate retryTemplate = new RetryTemplate();

			SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(RETRY_LIMIT);
			retryTemplate.setRetryPolicy(retryPolicy);

			FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
			backOffPolicy.setBackOffPeriod(1000); // 1초 대기 후 재시도
			retryTemplate.setBackOffPolicy(backOffPolicy);

			retryTemplate.execute(context -> {
				try {
					pkService.runPkTermRankingScheduler(LocalDate.now());
					log.info("[PK TERM] 포인트 랭킹 등록/유저 포인트 초기화 성공");
				} catch (Exception e) {
					log.warn("[PK TERM] 재시도 {}/{} 실패: {}", context.getRetryCount(), RETRY_LIMIT, e.getMessage());
					throw e;
				}
				return null;
			});

			return RepeatStatus.FINISHED;
		};
	}

}
