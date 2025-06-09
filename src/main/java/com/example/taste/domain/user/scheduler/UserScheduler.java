package com.example.taste.domain.user.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.taste.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserScheduler {

	private final UserRepository userRepository;

	@Scheduled(cron = "0 0 0 1 * *")
	public void resetPostingCnt() {
		userRepository.resetPostingCnt();
	}
}
