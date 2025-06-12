package com.example.taste.domain.notification.aop;

import java.time.LocalDateTime;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import com.example.taste.common.service.RedisService;
import com.example.taste.common.util.EntityFetcher;
import com.example.taste.domain.notification.NotificationCategory;
import com.example.taste.domain.notification.dto.NotificationEventDto;
import com.example.taste.domain.user.entity.User;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Aspect
@Component
public class boardAop {
	private final RedisService redisService;
	private final EntityFetcher entityFetcher;

	@AfterReturning(
		pointcut = "execution(* com.example.taste.domain.board.service.BoardService.createBoard(..))",
		returning = "result"
	)

	public void afterCreation(JoinPoint point, Object result) {
		Object[] args = point.getArgs();
		Long userId = (Long)args[0];
		User user = entityFetcher.getUserOrThrow(userId);
		Long boardId = (long)result;

		NotificationEventDto dto = NotificationEventDto.builder()
			.category(NotificationCategory.SUBSCRIBERS)
			.content(user.getNickname() + "님의 새 글이 등록되었습니다.")
			.createdAt(LocalDateTime.now())
			.read(false)
			.redirectUrl("/board/" + boardId) // 일단 그냥 board에 redirect. 이건 회의좀 해봐야 함.
			.userId(userId)
			.build();

		redisService.publishNotification(dto);
	}
}
