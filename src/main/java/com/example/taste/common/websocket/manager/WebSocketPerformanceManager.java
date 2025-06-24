package com.example.taste.common.websocket.manager;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class WebSocketPerformanceManager {
	private final Set<Long> sessionUserSet = ConcurrentHashMap.newKeySet();

	public void add(Long userId) {
		sessionUserSet.add(userId);
	}

	public void remove(Long userId) {
		sessionUserSet.remove(userId);
	}

	public int getCount() {
		return sessionUserSet.size();
	}
}
