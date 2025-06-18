// package com.example.taste.domain.notification.aop;
//
// import java.time.LocalDateTime;
// import java.util.Optional;
//
// import org.aspectj.lang.JoinPoint;
// import org.aspectj.lang.annotation.AfterReturning;
// import org.aspectj.lang.annotation.Aspect;
// import org.springframework.stereotype.Component;
//
// import com.example.taste.common.util.EntityFetcher;
// import com.example.taste.domain.board.entity.Board;
// import com.example.taste.domain.notification.NotificationCategory;
// import com.example.taste.domain.notification.dto.NotificationEventDto;
// import com.example.taste.domain.notification.redis.NotificationPublisher;
// import com.example.taste.domain.user.entity.User;
//
// import lombok.RequiredArgsConstructor;
//
// @RequiredArgsConstructor
// @Aspect
// @Component
// public class EventAop {
// 	private final NotificationPublisher notificationPublisher;
// 	private final EntityFetcher entityFetcher;
//
// 	@AfterReturning(
// 		pointcut = "execution(* com.example.taste.domain.event.service.EventService.findWinningBoard(..))",
// 		returning = "result"
// 	)
//
// 	public void afterCreation(JoinPoint point, Object result) {
// 		if (!(result instanceof Optional<?> optional) || optional.isEmpty()
// 			|| !(optional.get() instanceof Board board)) {
// 			return;
// 		}
//
// 		User user = board.getUserId();
// 		Long boardId = board.getId();
// 		Long userId = user.getId();
//
// 		NotificationEventDto eventDto = NotificationEventDto.builder()
// 			.category(NotificationCategory.INDIVIDUAL)
// 			.content(user.getNickname() + "님의 게시글이 이벤트에 당첨됐습니다.")
// 			.createdAt(LocalDateTime.now())
// 			.read(false)
// 			.redirectUrl("/board/" + boardId)
// 			.userId(userId)
// 			.build();
//
// 		notificationPublisher.publish(eventDto);
// 	}
// }
