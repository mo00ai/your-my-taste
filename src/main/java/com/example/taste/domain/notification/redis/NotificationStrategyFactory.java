package com.example.taste.domain.notification.redis;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.example.taste.domain.notification.entity.enums.NotificationCategory;
import com.example.taste.domain.notification.redis.strategy.NotificationStrategy;

@Component
public class NotificationStrategyFactory {

	private final Map<NotificationCategory, NotificationStrategy> strategyMap = new EnumMap<>(
		NotificationCategory.class);

	public NotificationStrategyFactory(List<NotificationStrategy> strategies) {
		// 전략 클래스마다 지원하는 카테고리를 선언해두고, 자동으로 매핑
		for (NotificationStrategy strategy : strategies) {
			if (strategy instanceof CategorySupport support) {
				for (NotificationCategory category : support.getSupportedCategories()) {
					strategyMap.put(category, strategy);
				}
			}
		}
	}

	public NotificationStrategy getStrategy(NotificationCategory category) {
		return strategyMap.getOrDefault(category, dto -> {
			// 기본 전략 또는 무시
		});
	}
}
