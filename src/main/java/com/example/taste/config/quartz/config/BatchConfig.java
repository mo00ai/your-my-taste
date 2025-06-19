package com.example.taste.config.quartz.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class BatchConfig {

	@Bean
	public Job testJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
		return new JobBuilder("testJob1", jobRepository)
			.start(testStep(jobRepository, transactionManager))
			.build();
	}

	@Bean
	public Step testStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
		return new StepBuilder("testStep", jobRepository)
			.tasklet(testTasklet(), transactionManager)
			.build();
	}

	@Bean
	public Tasklet testTasklet() {
		return (contribution, chunkContext) -> {
			System.out.println(">>>>>> Batch 실행됨!");
			return RepeatStatus.FINISHED;
		};
	}
}