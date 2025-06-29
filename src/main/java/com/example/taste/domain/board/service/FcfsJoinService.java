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
import com.example.taste.domain.board.entity.Board;
import com.example.taste.domain.board.repository.FcfsInformationRepository;
import com.example.taste.domain.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FcfsJoinService {

	private final RedisService redisService;
	private final RedissonClient redissonClient;
	private final SimpMessagingTemplate messagingTemplate;
	private final FcfsInformationRepository fcfsInformationRepository;
	private final FcfsPersistService fcfsPersistService;

	// 선착순 queue에 데이터 insert
	public void tryEnterFcfsQueue(Board board, Long userId) {
		String key = OPENRUN_KEY_PREFIX + board.getId();

		// 순위가 없는 유저만 ZSet에 insert
		if (redisService.hasRankInZSet(key, userId)) {
			return;
		}

		// ZSet 크기가 open limit을 초과하면 error로 메시지 전달
		long size = redisService.getZSetSize(key);
		if (board.isOverOpenLimit(size)) {
			throw new CustomException(EXCEED_OPEN_LIMIT);
		}

		redisService.addToZSet(key, userId, System.currentTimeMillis());

		// 클라이언트에 잔여 인원 전송
		String destination = "/sub/openrun/board/" + board.getId();
		long remainingSlot = Math.max(0, board.getOpenLimit() - redisService.getZSetSize(key));
		messagingTemplate.convertAndSend(destination, remainingSlot);

		// 동시성 문제로 openLimit 보다 초과 저장된 데이터 삭제
		Long rank = redisService.getRank(key, userId);
		if (rank != null && board.isOverOpenLimit(rank)) {
			redisService.removeFromZSet(key, userId);
			throw new CustomException(EXCEED_OPEN_LIMIT);
		}
	}

	// lettuce 분산락으로 동시성 제어
	public void tryEnterFcfsQueueByLettuce(Board board, Long userId) {
		String key = OPENRUN_KEY_PREFIX + board.getId();
		String lockKey = OPENRUN_LOCK_KEY_PREFIX + board.getId();

		if (redisService.hasRankInZSet(key, userId)) {
			return;
		}

		boolean hasLock = redisService.setIfAbsent(lockKey, userId.toString(), Duration.ofMillis(3000));
		int retry = 10;

		try {
			while (!hasLock && retry > 0) {
				Thread.sleep(50);
				hasLock = redisService.setIfAbsent(lockKey, userId, Duration.ofMillis(3000));
				retry--;
			}

			if (!hasLock) {
				throw new CustomException(REDIS_FAIL_GET_LOCK);
			}

			long size = redisService.getZSetSize(key);
			if (board.isOverOpenLimit(size)) {
				throw new CustomException(EXCEED_OPEN_LIMIT);
			}

			redisService.addToZSet(key, userId, System.currentTimeMillis());

			String destination = "/sub/openrun/board/" + board.getId();
			long remainingSlot = Math.max(0, board.getOpenLimit() - redisService.getZSetSize(key));
			messagingTemplate.convertAndSend(destination, remainingSlot);

		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new CustomException(REDIS_FAIL_GET_LOCK);

		} finally {
			Long value = redisService.getKeyLongValue(lockKey);
			if (value != null && value.equals(userId)) {
				redisService.deleteKey(lockKey);
			}
		}
	}

	// Redisson 분산락으로 동시성 제어
	public void tryEnterFcfsQueueByRedisson(Board board, User user) {
		String key = OPENRUN_KEY_PREFIX + board.getId();
		String lockKey = OPENRUN_LOCK_KEY_PREFIX + board.getId();

		// 순위가 이미 존재하는 유저는 바로 리턴
		if (redisService.hasRankInZSet(key, user.getId())) {
			return;
		} else if (fcfsInformationRepository.existsByBoardIdAndUserId(board.getId(), user.getId())) {
			return;
		} // 이미 순위가 다 차서 DB에 선착순 정보가 저장된 게시글이면 error
		else if (fcfsInformationRepository.existsByBoardId(board.getId())) {
			throw new CustomException(EXCEED_OPEN_LIMIT);
		}

		RLock lock = redissonClient.getLock(lockKey);

		try {
			boolean hasLock = lock.tryLock(1000, 3000, TimeUnit.MILLISECONDS);// 최대 1초 동안 락 획득 시도, 락 유지 시간 1초
			if (!hasLock) {
				throw new CustomException(REDIS_FAIL_GET_LOCK);
			}

			// ZSet 크기가 open limit을 초과하면 error로 메시지 전달
			long size = redisService.getZSetSize(key);
			if (board.isOverOpenLimit(size)) {
				fcfsPersistService.saveFcfsInfoToDB(key, board); // redis 메모리 관리(인원 다 차면 db에 삽입하고 key
				redisService.deleteKey(key);
				throw new CustomException(EXCEED_OPEN_LIMIT);
			}

			redisService.addToZSet(key, user.getId(), System.currentTimeMillis());

			// 클라이언트에 잔여 인원 전송
			String destination = "/sub/openrun/board/" + board.getId();
			long remainingSlot = Math.max(0, board.getOpenLimit() - redisService.getZSetSize(key));
			messagingTemplate.convertAndSend(destination, remainingSlot);

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
