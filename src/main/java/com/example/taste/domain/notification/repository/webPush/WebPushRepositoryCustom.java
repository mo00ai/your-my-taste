package com.example.taste.domain.notification.repository.webPush;

import java.util.List;

import com.example.taste.domain.notification.entity.WebPushSubscription;
import com.example.taste.domain.user.entity.User;

public interface WebPushRepositoryCustom {
	public WebPushSubscription getWebPushInformationByUserIdAndEndPoint(Long userId, String endPoint);

	public List<WebPushSubscription> findByUser(User user);
}
