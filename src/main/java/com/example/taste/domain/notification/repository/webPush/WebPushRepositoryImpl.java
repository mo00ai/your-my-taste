package com.example.taste.domain.notification.repository.webPush;

import com.example.taste.domain.notification.entity.QWebPushInformation;
import com.example.taste.domain.notification.entity.WebPushInformation;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class WebPushRepositoryImpl implements WebPushRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public WebPushInformation getWebPushInformationByUserIdAndEndPoint(Long userId, String endPoint) {
		QWebPushInformation qInfo = QWebPushInformation.webPushInformation;
		return queryFactory.selectFrom(qInfo).where(
			qInfo.endpoint.eq(endPoint),
			qInfo.user.id.eq(userId)
		).fetchOne();
	}
}
