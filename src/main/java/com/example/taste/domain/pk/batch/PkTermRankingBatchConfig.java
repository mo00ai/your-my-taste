package com.example.taste.domain.pk.batch;

import java.time.LocalDate;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import com.example.taste.domain.pk.service.PkService;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class PkTermRankingBatchConfig extends DefaultBatchConfiguration {

	private final PkService pkService;

	@Bean
	public Job pkTermRankingJob(JobRepository jobRepository, Step pkTermRankingStep) {
		return new JobBuilder("pkTermRankingJob", jobRepository)
			.start(pkTermRankingStep)
			.build();
	}

	@Bean
	public Step pkTermRankingStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
		return new StepBuilder("pkTermRankingStep", jobRepository)
			.tasklet((contribution, chunkContext) -> {
				pkService.runPkTermRankingScheduler(LocalDate.now());
				return RepeatStatus.FINISHED;
			}, transactionManager)
			.build();
	}

}
