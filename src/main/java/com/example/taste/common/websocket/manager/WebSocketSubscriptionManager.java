package com.example.taste.common.websocket.manager;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketSubscriptionManager {
	private final Map<Long, Set<String>> subMap = new ConcurrentHashMap<>();    // userId:{destinations}

	public void add(Long userId, String destination) {
		subMap.computeIfAbsent(userId, key -> ConcurrentHashMap.newKeySet())
			.add(destination);
	}

	public void remove(Long userId, String destination) {
		Set<String> destinationSet = subMap.get(userId);
		if (destinationSet != null) {
			destinationSet.remove(destination);
			if (destinationSet.isEmpty()) {
				subMap.remove(userId);
			}
		}
	}

	public void clear(Long userId) {
		subMap.remove(userId);
	}

	public boolean contains(Long userId, String destination) {
		Set<String> destinationSet = subMap.get(userId);
		return destinationSet != null && destinationSet.contains(destination);
	}
}
