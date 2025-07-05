// package com.example.taste.domain.pk.scheduler;
//
// import java.util.ArrayList;
// import java.util.HashMap;
// import java.util.List;
// import java.util.Map;
// import java.util.Set;
// import java.util.stream.Collectors;
//
// import org.springframework.data.redis.core.Cursor;
// import org.springframework.data.redis.core.ScanOptions;
// import org.springframework.stereotype.Component;
//
// import com.example.taste.common.service.RedisService;
// import com.example.taste.domain.pk.dto.request.PkLogCacheDto;
// import com.example.taste.domain.pk.entity.PkLog;
// import com.example.taste.domain.pk.service.PkService;
// import com.example.taste.domain.user.entity.User;
// import com.example.taste.domain.user.repository.UserRepository;
//
// import io.micrometer.core.instrument.Counter;
// import io.micrometer.core.instrument.DistributionSummary;
// import io.micrometer.core.instrument.MeterRegistry;
// import io.micrometer.core.instrument.Timer;
// import jakarta.annotation.PostConstruct;
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
//
// @Slf4j
// @Component
// @RequiredArgsConstructor
// public class PkLogScheduler {
//
// 	private final PkService pkService;
// 	private final RedisService redisService;
// 	private final UserRepository userRepository;
// 	private final MeterRegistry meterRegistry;
// 	private Counter errorCounter;
// 	private DistributionSummary summary;
//
// 	@PostConstruct
// 	public void initMetrics() {
// 		this.errorCounter = Counter.builder("pklog_scheduler_errors")
// 			.description("Number of errors in PkLogScheduler")
// 			.register(meterRegistry);
//
// 		this.summary = DistributionSummary.builder("pklog_batch_size")
// 			.description("Number of pk logs processed per batch")
// 			.baseUnit("records")
// 			.register(meterRegistry);
// 	}
//
// 	// @Scheduled(cron = "0 */5 * * * *")
// 	// @Scheduled(cron = "0 0 0 * * *")
// 	// @Scheduled(cron = "0 */5 * * * *")
// 	public void insertPkLogs() {
// 		Timer.Sample sample = Timer.start(meterRegistry);
// 		log.info("[PK LOG] Redis → DB Bulk insert 스케줄러 시작");
//
// 		ScanOptions options = ScanOptions.scanOptions().match("pkLog:*").count(1000).build();
// 		Map<String, Long> keyToUserId = new HashMap<>();
//
// 		try (Cursor<byte[]> cursor = redisService.getRedisConnection().keyCommands().scan(options)) {
// 			while (cursor.hasNext()) {
// 				String key = new String(cursor.next());
// 				keyToUserId.put(key, getUserIdFromKey(key));
// 			}
// 		} catch (Exception e) {
// 			log.error("[PK LOG] Redis 키 조회 실패", e);
// 			errorCounter.increment();
// 			return;
// 		}
//
// 		// 1. 유저 한 번에 로딩
// 		Map<Long, User> userMap = fetchUsersInBatches(keyToUserId.values().stream().collect(Collectors.toSet()));
//
// 		// 2. 전체 로그 수집
// 		List<PkLog> allPkLogs = new ArrayList<>();
//
// 		for (Map.Entry<String, Long> entry : keyToUserId.entrySet()) {
// 			String key = entry.getKey();
// 			Long userId = entry.getValue();
//
// 			User user = userMap.get(userId);
// 			if (user == null) {
// 				log.warn("[PK LOG] 존재하지 않는 유저. key={}, userId={}", key, userId);
// 				errorCounter.increment();
// 				continue;
// 			}
//
// 			try {
// 				List<PkLogCacheDto> dtoList = redisService.getOpsForList(key, PkLogCacheDto.class);
//
// 				List<PkLog> pkLogs = dtoList.stream()
// 					.map(dto -> PkLog.builder()
// 						.user(user)
// 						.pkType(dto.getPkType())
// 						.point(dto.getPoint())
// 						.createdAt(dto.getCreatedAt())
// 						.build())
// 					.toList();
//
// 				allPkLogs.addAll(pkLogs);
// 				summary.record(pkLogs.size());
// 			} catch (Exception e) {
// 				errorCounter.increment();
// 			}
// 		}
//
// 		// 3. insert는 한 번에
// 		if (!allPkLogs.isEmpty()) {
// 			try {
// 				pkService.saveBulkPkLogs(allPkLogs);
// 				log.info("[PK LOG] 전체 {}건 insert 완료", allPkLogs.size());
// 			} catch (Exception e) {
// 				log.error("[PK LOG] insertPkLogs 실패", e);
// 				errorCounter.increment();
// 			}
// 		}
//
// 		log.info("[PK LOG] Redis → DB Bulk insert 스케줄러 완료");
// 		sample.stop(meterRegistry.timer("pklog_scheduler_duration"));
// 	}
//
// 	private Map<Long, User> fetchUsersInBatches(Set<Long> userIds) {
// 		Map<Long, User> userMap = new HashMap<>();
// 		List<Long> idList = new ArrayList<>(userIds);
// 		int batchSize = 10_000; // 안전하게 1000 정도로
//
// 		for (int i = 0; i < idList.size(); i += batchSize) {
// 			List<Long> batch = idList.subList(i, Math.min(i + batchSize, idList.size()));
// 			userRepository.findAllById(batch).forEach(user -> userMap.put(user.getId(), user));
// 		}
//
// 		return userMap;
// 	}
//
// 	private Long getUserIdFromKey(String key) {
// 		String[] parts = key.split(":");
// 		return Long.parseLong(parts[2]);
// 	}
//
// }
