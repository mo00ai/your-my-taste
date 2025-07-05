package com.example.taste.common.websocket.manager;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Metrics;
import jakarta.annotation.PostConstruct;

@Component
public class OpenRunPerformanceManager {
	private final Set<Long> sessionSet = ConcurrentHashMap.newKeySet();

	@PostConstruct
	public void initGauge() {
		Gauge.builder("openrun_session_count", sessionSet, Set::size)
			.description("현재 오픈런 기능 커넥션 수")
			.register(Metrics.globalRegistry);
	}

	public void add(Long userId) {
		sessionSet.add(userId);
	}

	public void remove(Long userId) {
		sessionSet.remove(userId);
	}
}
