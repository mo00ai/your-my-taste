package com.example.taste.domain.party.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import com.example.taste.common.service.RedisService;
import com.example.taste.domain.party.entity.Party;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class PartyBatchConfig extends DefaultBatchConfiguration {
	private final RedisService redisService;

	private static final int CHUNK_SIZE = 100;
	private static final int RETRY_LIMIT = 3;

	@Bean
	public Job updatePartyJob(
		JobRepository repo, Step updateExpiredPartyStep, Step updateSoftDeletePartyStep) {
		return new JobBuilder("UpdatePartyBatchJob", repo)
			.start(updateExpiredPartyStep)
			.next(updateSoftDeletePartyStep)
			.build();
	}

	@Bean
	public Step updateExpiredPartyStep(JobRepository repo, PlatformTransactionManager transactionManager) {
		return new StepBuilder("UpdateExpiredPartyStep", repo)
			.<String, String>chunk(CHUNK_SIZE, transactionManager)
			.build();
	}

	@Bean
	public Step updateSoftDeletePartyStep(JobRepository repo, PlatformTransactionManager transactionManager) {
		return new StepBuilder("UpdateSoftDeletePartyStep", repo)
			.<String, String>chunk(CHUNK_SIZE, transactionManager)
			.build();
	}

	@Bean
	@StepScope
	public ItemReader<Party> updateExpiredPartyReader() {
		return new JdbcPagingItemReaderBuilder<Party>()
			.pageSize(CHUNK_SIZE)
			.dataSource(getDataSource()).build();

	}

	// @Bean
	// @StepScope
	// public ItemWriter<String> updateExpiredPartyWriter() {
	//
	// }
}
