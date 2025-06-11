package com.example.taste.domain.user.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.taste.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserScheduler {

	private final UserRepository userRepository;

	// todo : 재시도 로직 구현, JPQL은 native query와 다르게 내부적으로 트랜잭션 동작(JPA가 깊게 간섭 가능)하므로 전체 롤백 가능성 고려
	@Scheduled(cron = "0 0 0 1 * *")
	public void resetPostingCnt() {
		userRepository.resetPostingCnt();
	}
}
