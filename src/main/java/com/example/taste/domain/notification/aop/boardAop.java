package com.example.taste.domain.notification.aop;

import java.time.LocalDateTime;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import com.example.taste.common.util.EntityFetcher;
import com.example.taste.domain.notification.NotificationCategory;
import com.example.taste.domain.notification.dto.NotificationEventDto;
import com.example.taste.domain.notification.redis.NotificationPublisher;
import com.example.taste.domain.user.entity.User;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Aspect
@Component
public class boardAop {
	private final NotificationPublisher notificationPublisher;
	private final EntityFetcher entityFetcher;

	// 게시글 생성 완료 후
	@AfterReturning(
		pointcut = "execution(* com.example.taste.domain.board.service.BoardService.createBoard(..))",
		returning = "result"
	)

	public void afterCreation(JoinPoint point, Object result) {
		if (result == null) {
			return;
		}
		// service 가 받은 매개변수들
		Object[] args = point.getArgs();
		// 중에 1번째가 userId임
		Long userId = (Long)args[0];
		// 게시글 작성 유저
		User user = entityFetcher.getUserOrThrow(userId);
		// 해당 게시글 id
		Long boardId = (long)result;

		NotificationEventDto eventDto = NotificationEventDto.builder()
			.category(NotificationCategory.SUBSCRIBERS)
			.content(user.getNickname() + "님의 새 글이 등록되었습니다.")
			.createdAt(LocalDateTime.now())
			.read(false)
			.redirectUrl("/board/" + boardId)
			.userId(userId)
			.build();

		notificationPublisher.publish(eventDto);
	}
}
