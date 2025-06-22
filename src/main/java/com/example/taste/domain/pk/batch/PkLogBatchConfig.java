package com.example.taste.domain.pk.batch;

import static com.example.taste.domain.user.exception.UserErrorCode.*;

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
			.build();
	}

	@Bean
	@StepScope
	public ItemReader<String> pkLogKeyReader() {
		List<String> keys = RetryUtils.executeWithRetry(
			() -> redisService.getKeys("pkLog:*").stream().toList(),
			RETRY_LIMIT,
			"Reader - [PK LOG] 키 스캔");

		log.info("[PK LOG] {}개의 키를 조회함", keys.size());
		return new ListItemReader<>(keys);
	}

	@Bean
	@StepScope
	public ItemWriter<String> pkLogWriter() {
		return keys -> {
			for (String key : keys) {
				RetryUtils.executeWithRetry(() -> {
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
					log.info("[PK LOG] {}에 대해 {}건 저장", key, pkLogs.size());
					return null;
				}, RETRY_LIMIT, "Writer - [PK LOG] 쓰기 - key: " + key);
			}
		};
	}
}
