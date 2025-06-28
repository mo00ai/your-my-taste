package com.example.taste.domain.party.batch;

import java.time.LocalDate;
import java.util.List;

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
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.IteratorItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.PlatformTransactionManager;

import com.example.taste.domain.party.repository.PartyRepository;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class PartyBatchConfig extends DefaultBatchConfiguration {

	private static final int CHUNK_SIZE = 1000;
	private static final int RETRY_LIMIT = 3;

	@Bean(name = "updatePartyJob")
	public Job updatePartyJob(
		JobRepository repo, Step updateExpiredPartyStep, Step updateSoftDeletePartyStep) {
		return new JobBuilder("updatePartyJob", repo)
			.start(updateExpiredPartyStep)
			.next(updateSoftDeletePartyStep)
			.build();
	}

	@Bean
	public Step updateExpiredPartyStep(JobRepository repo, PlatformTransactionManager transactionManager,
		PartyRepository partyRepository) {
		return new StepBuilder("updateExpiredPartyStep", repo)
			.<Long, Long>chunk(CHUNK_SIZE, transactionManager)
			.reader(updateExpiredPartyReader(partyRepository))
			.writer(updateExpiredPartyWriter(partyRepository))
			.faultTolerant()
			.retry(DataAccessException.class)
			.retryLimit(RETRY_LIMIT)
			.build();
	}

	@Bean
	public Step updateSoftDeletePartyStep(JobRepository repo, PlatformTransactionManager transactionManager,
		PartyRepository partyRepository) {
		return new StepBuilder("updateSoftDeletePartyStep", repo)
			.<Long, Long>chunk(CHUNK_SIZE, transactionManager)
			.reader(updateSoftDeletePartyReader(partyRepository))
			.writer(updateSoftDeletePartyWriter(partyRepository))
			.faultTolerant()
			.retry(DataAccessException.class)
			.retryLimit(RETRY_LIMIT)
			.build();
	}

	@Bean
	@StepScope
	public ItemReader<Long> updateExpiredPartyReader(PartyRepository partyRepository) {
		LocalDate before1days = LocalDate.now().minusDays(1);
		List<Long> expiredPartyIds = partyRepository.findAllByMeetingDate(before1days);
		log.debug("[UpdateExpiredPartyReader] 만료된 파티 ids: {}", expiredPartyIds);
		return new IteratorItemReader<>(expiredPartyIds);
	}

	@Bean
	@StepScope
	public ItemWriter<Long> updateExpiredPartyWriter(PartyRepository partyRepository) {
		return items -> {
			if (items.isEmpty()) {
				return;
			}

			long updatedCount = partyRepository.updateExpiredStateByIds(items.getItems());
			log.info("[UpdateExpiredPartyWriter] {}개의 파티를 EXPIRED 상태로 업데이트 완료함", updatedCount);
		};
	}

	@Bean
	@StepScope
	public ItemReader<Long> updateSoftDeletePartyReader(PartyRepository partyRepository) {
		LocalDate before7days = LocalDate.now().minusDays(7);
		List<Long> expiredPartyIds = partyRepository.findAllByMeetingDate(before7days);
		log.debug("[UpdateSoftDeletePartyReader] 삭제된 파티 ids: {}", expiredPartyIds);
		return new IteratorItemReader<>(expiredPartyIds);
	}

	@Bean
	@StepScope
	public ItemWriter<Long> updateSoftDeletePartyWriter(PartyRepository partyRepository) {
		return items -> {
			if (items.isEmpty()) {
				return;
			}

			long updatedCount = partyRepository.softDeleteByIds(items.getItems());
			log.info("[UpdateSoftDeletePartyWriter] {}개의 파티를 삭제 상태로 업데이트 완료함", updatedCount);
		};
	}
}
