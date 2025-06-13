package com.example.taste.domain.match.service;

import lombok.RequiredArgsConstructor;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.taste.domain.match.dto.MatchEvent;
import com.example.taste.domain.match.enums.MatchJobType;
import com.example.taste.domain.match.redis.MatchPublisher;

@Service
@RequiredArgsConstructor
public class MatchScheduler {
	private final MatchPublisher matchPublisher;

	// 5분마다 실행
	@Scheduled(cron = "0 0/5 * * * ?")
	public void runScheduledMatch() {
		matchPublisher.publish(new MatchEvent(MatchJobType.PARTY_MATCH));
	}
}
