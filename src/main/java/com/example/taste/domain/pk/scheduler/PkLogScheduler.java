package com.example.taste.domain.pk.scheduler;

import static com.example.taste.domain.user.exception.UserErrorCode.*;

import java.util.List;

import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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
@Component
@RequiredArgsConstructor
public class PkLogScheduler {

	private final PkService pkService;
	private final RedisService redisService;
	private final UserRepository userRepository;

	@Scheduled(cron = "0 0 0 * * *")
	public void insertPkLogs() {

		log.info("[PK LOG] Redis → DB Bulk insert 스케줄러 시작");

		ScanOptions options = ScanOptions.scanOptions().match("pkLog:*").count(100).build();

		try (Cursor<byte[]> cursor = redisService.getRedisConnection().keyCommands().scan(options)) {
			while (cursor.hasNext()) {

				String key = new String(cursor.next());

				Long userId = getUserIdFromKey(key);

				User user = userRepository.findById(userId)
					.orElseThrow(() -> new CustomException(USER_NOT_FOUND));

				List<PkLogCacheDto> dtoList = redisService.getOpsForList(key, PkLogCacheDto.class);

				List<PkLog> pkLogs = dtoList.stream()
					.map(dto -> PkLog.builder()
						.user(user)
						.pkType(dto.getPkType())
						.point(dto.getPoint())
						.createdAt(dto.getCreatedAt())
						.build()
					).toList();

				pkService.saveBulkPkLogs(pkLogs);
			}

			log.info("[PK LOG] Redis → DB Bulk insert 스케줄러 완료");

		} catch (Exception e) {

			log.error("[PK LOG] Redis → DB Bulk insert 실패", e);

			// Todo 재시도 로직 또는 Retryable, Log 추적(테이블 따로 만듦), 통합 스케줄러 관리 등

		}
	}

	private Long getUserIdFromKey(String key) {
		String[] parts = key.split(":");
		return Long.parseLong(parts[2]);
	}

}
