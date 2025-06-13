package com.example.taste.domain.match.aop;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.example.taste.domain.match.annotation.MatchEventPublish;
import com.example.taste.domain.match.dto.MatchEvent;
import com.example.taste.domain.match.redis.MatchPublisher;

@Aspect
@Configuration
@RequiredArgsConstructor
public class MatchEventPublishAspect {
	private final MatchPublisher matchPublisher;

	@AfterReturning(
		pointcut = "@annotation(matchEventPublish)",
		returning = "userMatchCondIds")
	public void publishMatchAfterReturn(JoinPoint joinPoint,
		List<Long> userMatchCondIds, MatchEventPublish matchEventPublish) throws Throwable {
		TransactionSynchronizationManager.registerSynchronization(
			new TransactionSynchronizationAdapter() {
				// 반드시 커밋 이후에
				@Override
				public void afterCommit() {
					switch (matchEventPublish.matchJobType()) {
						case USER_MATCH -> {
							if (userMatchCondIds == null || userMatchCondIds.isEmpty()) {
								return;
							}
							matchPublisher.publish(
								new MatchEvent(matchEventPublish.matchJobType(), userMatchCondIds));
						}
						case PARTY_MATCH -> {
							matchPublisher.publish(
								new MatchEvent(matchEventPublish.matchJobType()));
						}
					}
				}
			});
	}
}
