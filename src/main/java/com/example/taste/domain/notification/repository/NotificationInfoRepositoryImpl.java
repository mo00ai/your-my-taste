package com.example.taste.domain.notification.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import com.example.taste.domain.notification.entity.NotificationInfo;
import com.example.taste.domain.notification.entity.QNotificationContent;
import com.example.taste.domain.notification.entity.QNotificationInfo;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class NotificationInfoRepositoryImpl implements NotificationInfoRepositoryCustom {
	private final JPAQueryFactory queryFactory;

	@Override
	public Slice<NotificationInfo> getMoreNotificationInfoWithContents(Long userId, List<Long> redisNotifications,
		Pageable pageable) {
		QNotificationInfo qNotificationInfo = QNotificationInfo.notificationInfo;

		List<NotificationInfo> notificationInfos = queryFactory.selectFrom(qNotificationInfo)
			.where(
				redisNotifications.isEmpty() ? null :
					qNotificationInfo.createdAt.before(LocalDateTime.now().minusDays(7)),
				qNotificationInfo.user.id.eq(userId)
			)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize() + 1)
			.fetch();

		boolean hasNext = notificationInfos.size() > pageable.getPageSize();
		if (hasNext) {
			notificationInfos.remove(notificationInfos.size() - 1);
		}
		return new SliceImpl<>(notificationInfos, pageable, hasNext);
	}

	@Override
	public List<NotificationInfo> getNotificationInfoWithContents(Long userId, List<Long> contentsIds) {
		QNotificationInfo qNotificationInfo = QNotificationInfo.notificationInfo;
		QNotificationContent qNotificationContent = QNotificationContent.notificationContent;
		return queryFactory.selectFrom(qNotificationInfo)
			.leftJoin(qNotificationInfo.notificationContent, qNotificationContent)
			.fetchJoin()
			.where(
				contentsIds.isEmpty() ? null :
					qNotificationInfo.notificationContent.id.in(contentsIds),
				qNotificationInfo.user.id.eq(userId)
			)
			.fetch();
	}
}
