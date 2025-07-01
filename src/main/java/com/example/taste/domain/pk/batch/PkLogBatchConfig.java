package com.example.taste.domain.pk.batch;

import static com.example.taste.domain.user.exception.UserErrorCode.*;

import java.util.List;

import org.jooq.exception.DataAccessException;
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
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.transaction.PlatformTransactionManager;

import com.example.taste.common.exception.CustomException;
import com.example.taste.common.service.RedisService;
import com.example.taste.domain.pk.dto.request.PkLogCacheDto;
import com.example.taste.domain.pk.entity.PkLog;
import com.example.taste.domain.pk.service.PkService;
import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class PkLogBatchConfig extends DefaultBatchConfiguration {

	private final PkService pkService;
	private final RedisService redisService;
	private final UserRepository userRepository;
	private static final int RETRY_LIMIT = 3;

	@Bean
	public Job pkLogJob(JobRepository repo, Step pkLogStep) {
		return new JobBuilder("PkLogBatchJob", repo)
			.start(pkLogStep)
			.build();
	}

	@Bean
	public Step pkLogStep(JobRepository repo, PlatformTransactionManager transactionManager) {
		return new StepBuilder("pkLogStep", repo)
			.<String, String>chunk(100, transactionManager)
			.reader(pkLogKeyReader())
			.writer(pkLogWriter())
			.faultTolerant()
			.retry(DataAccessException.class)
			.retryLimit(3)
			.listener(new RetryListener() {
				@Override
				public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback,
					Throwable throwable) {
					int retryCount = context.getRetryCount();
					int retryLimit = 3;
					log.warn("[PK LOG] 저장 실패. 재시도 {}/{}", retryCount, retryLimit);

					if (retryCount >= retryLimit) {
						log.error("[PK LOG] 재시도 한계 초과. 최종 실패", throwable);
					}
				}
			})
			.skip(EmptyResultDataAccessException.class) // 조회한 id가 존재하지 않을 때 (writer 호출 전 삭제됨)
			.build();
	}

	@Bean
	@StepScope
	public ItemReader<String> pkLogKeyReader() {

		List<String> keys = redisService.getKeys("pkLog:*").stream().toList();
		log.info("[PK LOG] {}개의 키 조회", keys.size());
		return new ListItemReader<>(keys);

	}

	@Bean
	@StepScope
	public ItemWriter<String> pkLogWriter() {
		return keys -> {
			for (String key : keys) {

				Long userId = Long.parseLong(key.split(":")[2]);
				User user = userRepository.findById(userId)
					.orElseThrow(() -> new CustomException(NOT_FOUND_USER));

				List<PkLogCacheDto> dtoList = redisService.getOpsForList(key, PkLogCacheDto.class);

				List<PkLog> pkLogs = dtoList.stream()
					.map(dto -> PkLog.builder()
						.user(user)
						.pkType(dto.getPkType())
						.point(dto.getPoint())
						.createdAt(dto.getCreatedAt())
						.build())
					.toList();

				pkService.saveBulkPkLogs(pkLogs);
				log.info("[PK LOG] {}에 대해 {}건 저장 완료", key, pkLogs.size());
			}
		};
	}
}
