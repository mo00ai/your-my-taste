package com.example.taste.domain.board.service;

import static com.example.taste.common.constant.RedisConst.*;
import static com.example.taste.common.exception.ErrorCode.*;
import static com.example.taste.domain.board.exception.BoardErrorCode.*;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.example.taste.common.exception.CustomException;
import com.example.taste.common.service.RedisService;
import com.example.taste.domain.board.dto.mq.BoardStatusDto;
import com.example.taste.domain.board.dto.response.BoardResponseDto;
import com.example.taste.domain.board.mq.BoardStatusPublisher;
import com.example.taste.domain.board.repository.FcfsInformationRepository;
import com.example.taste.domain.user.entity.User;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcfsQueueService {

	private final RedisService redisService;
	private final RedissonClient redissonClient;
	private final SimpMessagingTemplate messagingTemplate;
	private final FcfsInformationRepository fcfsInformationRepository;
	private final FcfsInformationService fcfsInformationService;
	private final BoardStatusPublisher boardStatusPublisher;
	private final MeterRegistry registry;
	private Counter baseCounter;
	private Counter lettuceCounter;
	private Counter redissonCounter;

	@PostConstruct
	void init() {
		baseCounter = Counter.builder("fcfs.success.base")
			.description("조건문 방식 FCFS 성공 카운터")
			.register(registry);

		lettuceCounter = Counter.builder("fcfs.success.lettuce")
			.description("Lettuce 락 방식 FCFS 성공 카운터")
			.register(registry);

		redissonCounter = Counter.builder("fcfs.success.redisson")
			.description("Redisson 락 방식 FCFS 성공 카운터")
			.register(registry);
	}

	// 선착순 queue에 데이터 insert
	public void tryEnterFcfsQueue(BoardResponseDto dto, User user) {
		String key = FCFS_KEY_PREFIX + dto.getBoardId();

		// 순위가 없는 유저만 ZSet에 insert
		if (redisService.hasRankInZSet(key, user.getId())) {
			return;
		}
		if (fcfsInformationRepository.existsByBoardIdAndUserId(dto.getBoardId(), user.getId())) {
			return;
		}
		if (fcfsInformationRepository.existsByBoardId(dto.getBoardId())) {
			throw new CustomException(EXCEED_OPEN_LIMIT);
		}

		// ZSet 크기가 open limit을 초과하면 error로 메시지 전달
		long size = redisService.getZSetSize(key);
		if (dto.getOpenLimit() <= size) {
			fcfsInformationService.saveFcfsInfoToDB(key, dto.getBoardId());
			redisService.deleteKey(key);
			throw new CustomException(EXCEED_OPEN_LIMIT);
		}

		redisService.addToZSet(key, user.getId(), System.currentTimeMillis());

		// 클라이언트에 잔여 인원 전송
		//String destination = BOARD_SOCKET_DESTINATION + board.getId();
		long remainingSlot = Math.max(0, dto.getOpenLimit() - redisService.getZSetSize(key));
		//messagingTemplate.convertAndSend(destination, remainingSlot);
		boardStatusPublisher.publish(new BoardStatusDto(dto.getBoardId(), remainingSlot));

		// 동시성 문제로 openLimit 보다 초과 저장된 데이터 삭제
		Long rank = redisService.getRank(key, user.getId());
		if (rank != null && dto.getOpenLimit() <= rank) {
			redisService.removeFromZSet(key, user.getId());
			throw new CustomException(EXCEED_OPEN_LIMIT);
		}
		baseCounter.increment();
	}

	// lettuce 분산락으로 동시성 제어
	public void tryEnterFcfsQueueByLettuce(BoardResponseDto dto, User user) {
		String key = FCFS_KEY_PREFIX + dto.getBoardId();
		String lockKey = FCFS_LOCK_KEY_PREFIX + dto.getBoardId();

		// 순위권에 존재하는 유저 return
		if (redisService.hasRankInZSet(key, user.getId())) {
			return;
		}
		if (fcfsInformationRepository.existsByBoardIdAndUserId(dto.getBoardId(), user.getId())) {
			return;
		}

		boolean hasLock = redisService.setIfAbsent(lockKey, user.getId(), Duration.ofMillis(3000));
		int retry = 10;

		try {
			while (!hasLock && retry > 0) {
				Thread.sleep(50);
				hasLock = redisService.setIfAbsent(lockKey, user.getId(), Duration.ofMillis(3000));
				retry--;
			}

			if (!hasLock) {
				throw new CustomException(REDIS_FAIL_GET_LOCK);
			}

			if (fcfsInformationRepository.existsByBoardId(dto.getBoardId())) {
				throw new CustomException(EXCEED_OPEN_LIMIT);
			}

			// ZSet 크기가 open limit을 초과하면 error로 메시지 전달
			long size = redisService.getZSetSize(key);
			if (dto.getOpenLimit() <= size) {
				fcfsInformationService.saveFcfsInfoToDB(key, dto.getBoardId()); // redis 메모리 관리(인원 다 차면 db에 삽입하고 key
				redisService.deleteKey(key);
				throw new CustomException(EXCEED_OPEN_LIMIT);
			}

			redisService.addToZSet(key, user.getId(), System.currentTimeMillis());
			lettuceCounter.increment();

			// 클라이언트에 잔여 인원 전송
			//String destination = BOARD_SOCKET_DESTINATION + board.getId();
			long remainingSlot = Math.max(0, dto.getOpenLimit() - redisService.getZSetSize(key));
			//messagingTemplate.convertAndSend(destination, remainingSlot);
			boardStatusPublisher.publish(new BoardStatusDto(dto.getBoardId(), remainingSlot));

		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new CustomException(REDIS_FAIL_GET_LOCK);

		} finally {
			Long value = redisService.getKeyLongValue(lockKey);
			if (value != null && value.equals(user.getId())) {
				redisService.deleteKey(lockKey);
			}
		}
	}

	// Redisson 분산락으로 동시성 제어
	public void tryEnterFcfsQueueByRedisson(BoardResponseDto dto, User user) {
		String key = FCFS_KEY_PREFIX + dto.getBoardId();
		String lockKey = FCFS_LOCK_KEY_PREFIX + dto.getBoardId();

		// 순위가 이미 존재하는 유저는 바로 리턴
		if (redisService.hasRankInZSet(key, user.getId())) {
			return;
		}
		if (fcfsInformationRepository.existsByBoardIdAndUserId(dto.getBoardId(), user.getId())) {
			return;
		}

		RLock lock = redissonClient.getLock(lockKey);

		try {
			boolean hasLock = lock.tryLock(1000, 3000, TimeUnit.MILLISECONDS);// 최대 1초 동안 락 획득 시도, 락 유지 시간 3초
			if (!hasLock) {
				throw new CustomException(REDIS_FAIL_GET_LOCK);
			}

			if (fcfsInformationRepository.existsByBoardId(dto.getBoardId())) {
				throw new CustomException(EXCEED_OPEN_LIMIT);
			}

			// ZSet 크기가 open limit을 초과하면 error로 메시지 전달
			long size = redisService.getZSetSize(key);
			if (dto.getOpenLimit() <= size) {
				fcfsInformationService.saveFcfsInfoToDB(key, dto.getBoardId()); // redis 메모리 관리(인원 다 차면 db에 삽입하고 key
				redisService.deleteKey(key);
				throw new CustomException(EXCEED_OPEN_LIMIT);
			}

			redisService.addToZSet(key, user.getId(), System.currentTimeMillis());
			redissonCounter.increment();

			// 클라이언트에 잔여 인원 전송
			//String destination = BOARD_SOCKET_DESTINATION + board.getId();
			long remainingSlot = Math.max(0, dto.getOpenLimit() - redisService.getZSetSize(key));
			//messagingTemplate.convertAndSend(destination, remainingSlot);
			boardStatusPublisher.publish(
				new BoardStatusDto(dto.getBoardId(), remainingSlot)); // 메세지큐 적용 -> 메세지 전송 성능 향상

		} catch (InterruptedException e) {
			Thread.currentThread().interrupt(); // 스레드 중단 요청
			throw new CustomException(REDIS_FAIL_GET_LOCK);

		} finally {
			if (lock.isHeldByCurrentThread()) { // 현재 쓰레드가 락 소유자인지 확인
				lock.unlock();
			}
		}
	}
}
