package com.example.taste.domain.notification.repository.webPush;

import java.util.List;

import com.example.taste.domain.notification.entity.QWebPushSubscription;
import com.example.taste.domain.notification.entity.WebPushSubscription;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class WebPushRepositoryImpl implements WebPushRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public WebPushSubscription getWebPushSubscriptionByUserIdAndFcmToken(Long userId, String fcmToken) {
		QWebPushSubscription qSubscription = QWebPushSubscription.webPushSubscription;
		return queryFactory.selectFrom(qSubscription).where(
			qSubscription.fcmToken.eq(fcmToken),
			qSubscription.user.id.eq(userId)
		).fetchOne();
	}

	@Override
	public List<WebPushSubscription> findByUserId(Long userId) {
		QWebPushSubscription qWebPushSubscription = QWebPushSubscription.webPushSubscription;
		return queryFactory.selectFrom(qWebPushSubscription).where(
			qWebPushSubscription.user.id.eq(userId)
		).fetch();
	}
}
