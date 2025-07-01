package com.example.taste.domain.notification.repository.webPush;

import java.util.List;

import com.example.taste.domain.notification.entity.WebPushSubscription;

public interface WebPushRepositoryCustom {
	public WebPushSubscription getWebPushSubscriptionByUserIdAndEndpoint(Long userId, String endPoint);

	public List<WebPushSubscription> findByUserId(Long userId);
}
