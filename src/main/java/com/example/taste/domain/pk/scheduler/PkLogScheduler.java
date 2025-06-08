package com.example.taste.domain.pk.scheduler;

import static com.example.taste.domain.pk.exception.PkErrorCode.*;
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
import com.example.taste.domain.pk.repository.PkLogJdbcRepository;
import com.example.taste.domain.pk.service.PkService;
import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PkLogScheduler {

	private final PkService pkService;
	private final RedisService redisService;
	private final UserRepository userRepository;
	private final PkLogJdbcRepository pkLogJdbcRepository;

	@Scheduled(cron = "0 0 0 * * *")
	public void insertPkLogs() {

		ScanOptions options = ScanOptions.scanOptions().match("pkLog:*").count(100).build();

		try (Cursor<byte[]> cursor = redisService.getRedisConnection().keyCommands().scan(options)) {
			while (cursor.hasNext()) {

				String key = new String(cursor.next());

				List<PkLogCacheDto> pkLogDtos = redisService.getOpsForList(key, PkLogCacheDto.class);

				List<PkLog> pkLogs = pkLogDtos.stream().map(dto -> {

					User user = userRepository.findById(dto.getUserId())
						.orElseThrow(() -> new CustomException(USER_NOT_FOUND));

					return PkLog.builder()
						.pkType(dto.getPkType())
						.point(dto.getPoint())
						.createdAt(dto.getCreatedAt())
						.user(user)
						.build();
				}).toList();

				pkLogJdbcRepository.batchInsert(pkLogs);

			}
		} catch (Exception e) {
			throw new CustomException(PK_LOG_BULK_INSERT_FAILED);

			// Todo 재시도 로직 또는 Retryable, Log 추적(테이블 따로 만듦), 통합 스케줄러 관리 등

		}
	}

}
