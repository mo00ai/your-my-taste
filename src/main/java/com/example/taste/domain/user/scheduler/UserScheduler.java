package com.example.taste.domain.user.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.taste.domain.user.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserScheduler {

	private final UserRepository userRepository;

	// TODO 재시도 로직 구현, 전체 롤백 가능성 고려 @김채진
	@Transactional
	@Scheduled(cron = "0 0 0 1 * *")
	public void resetPostingCnt() {
		int updatedCount = userRepository.resetPostingCnt();
		log.info("[UserScheduler] 총 {}명 유저 포스팅 횟수 초기화 완료", updatedCount);
	}
}
