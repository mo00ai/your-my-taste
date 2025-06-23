package com.example.taste.domain.notification.repository.webPush;

import com.example.taste.domain.notification.entity.WebPushInformation;

public interface WebPushRepositoryCustom {
	public WebPushInformation getWebPushInformationByUserIdAndEndPoint(Long userId, String endPoint);
}
