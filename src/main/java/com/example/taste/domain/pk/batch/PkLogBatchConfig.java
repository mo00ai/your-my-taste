package com.example.taste.domain.pk.batch;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import com.example.taste.common.service.RedisService;
import com.example.taste.domain.pk.dto.request.PkLogCacheDto;
import com.example.taste.domain.pk.entity.PkLog;
import com.example.taste.domain.pk.service.PkService;
import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.repository.UserRepository;
import com.sun.management.OperatingSystemMXBean;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class PkLogBatchConfig extends DefaultBatchConfiguration {

	private final PkService pkService;
	private final RedisService redisService;
	private final UserRepository userRepository;
	private final RedisTemplate redisTemplate;
	private final MeterRegistry meterRegistry;
	private static final int RETRY_LIMIT = 3;

	@PostConstruct
	public void registerJvmGauges() {
		MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
		OperatingSystemMXBean osBean =
			(OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean();

		// Heap 메모리 사용량 (bytes)
		Gauge.builder("jvm.memory.heap.used", memoryBean, m -> (double)m.getHeapMemoryUsage().getUsed())
			.description("JVM Heap memory used")
			.baseUnit("bytes")
			.register(meterRegistry);

		// Heap 메모리 커밋량 (bytes)
		Gauge.builder("jvm.memory.heap.committed", memoryBean, m -> (double)m.getHeapMemoryUsage().getCommitted())
			.description("JVM Heap memory committed")
			.baseUnit("bytes")
			.register(meterRegistry);

		// 시스템 전체 CPU 사용률 (0.0 ~ 1.0)
		Gauge.builder("system.cpu.usage", osBean, OperatingSystemMXBean::getSystemCpuLoad)
			.description("System CPU usage")
			.register(meterRegistry);

		// JVM 프로세스의 CPU 사용률 (0.0 ~ 1.0)
		Gauge.builder("process.cpu.usage", osBean, OperatingSystemMXBean::getProcessCpuLoad)
			.description("Process CPU usage")
			.register(meterRegistry);
	}

	@Bean
	public TaskExecutor pkLogTaskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(4);     // 동시에 실행할 스레드 수
		executor.setMaxPoolSize(8);      // 최대 스레드 수
		executor.setQueueCapacity(100);  // 큐에 대기할 작업 수
		executor.setThreadNamePrefix("PkLogExecutor-");
		executor.initialize();
		return executor;
	}

	@Bean
	public Job pkLogJob(JobRepository repo, Step pkLogStep) {
		return new JobBuilder("PkLogBatchJob", repo)
			.start(pkLogStep)
			.build();
	}

	@Bean
	public Step pkLogStep(JobRepository repo, PlatformTransactionManager transactionManager,
		TaskExecutor pkLogTaskExecutor) {
		return new StepBuilder("pkLogStep", repo)
			.<String, String>chunk(1000, transactionManager)
			.reader(PkLogItemReader())
			.writer(pkLogWriter())
			.taskExecutor(pkLogTaskExecutor)
			.faultTolerant()
			.retry(DataAccessException.class)
			.retryLimit(3)
			.skip(EmptyResultDataAccessException.class) // 조회한 id가 존재하지 않을 때 (writer 호출 전 삭제됨)
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
			.build();
	}

	@Bean
	@StepScope
	public ItemReader<String> PkLogItemReader() {
		return new PkLogItemReader(redisTemplate, "pkLog:*");
	}

	@Bean
	@StepScope
	public ItemWriter<String> pkLogWriter() {
		return items -> {

			Timer.Sample sample = Timer.start(meterRegistry);

			List<? extends String> keys = items.getItems();

			// 1. userId 수집
			Set<Long> userIds = keys.stream()
				.map(key -> Long.parseLong(key.split(":")[2]))
				.collect(Collectors.toSet());

			// 2. batch 조회
			Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
				.collect(Collectors.toMap(User::getId, u -> u));

			// 3. 모든 PkLog 한 번에 수집
			List<PkLog> allPkLogs = new ArrayList<>();

			for (String key : keys) {
				Long userId = Long.parseLong(key.split(":")[2]);
				User user = userMap.get(userId);
				if (user == null)
					continue;

				List<PkLogCacheDto> dtoList = redisService.getOpsForList(key, PkLogCacheDto.class);

				List<PkLog> pkLogs = dtoList.stream()
					.map(dto -> PkLog.builder()
						.user(user)
						.pkType(dto.getPkType())
						.point(dto.getPoint())
						.createdAt(dto.getCreatedAt())
						.build())
					.toList();

				allPkLogs.addAll(pkLogs);
			}

			int batchSize = 10000;
			for (int i = 0; i < allPkLogs.size(); i += batchSize) {
				int end = Math.min(i + batchSize, allPkLogs.size());
				List<PkLog> subList = allPkLogs.subList(i, end);
				pkService.saveBulkPkLogs(subList);
			}

			sample.stop(meterRegistry.timer("pklog_batch_duration"));

		};
	}
}
