package com.example.taste.common.util;

import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class MemoryTracker {

	private final MeterRegistry meterRegistry;

	public void measureMemoryUsage(String taskName, Runnable task) {
		long before = getUsedMemory();
		task.run();
		long after = getUsedMemory();
		long used = after - before;

		DistributionSummary summary = DistributionSummary
			.builder("custom_memory_used_bytes")
			.description("Memory used per task")
			.baseUnit("bytes")
			.tag("task", taskName)
			.register(meterRegistry);

		summary.record(used); // ✅ 매번 기록됨

		log.info("[{}] 사용한 메모리: {} bytes", taskName, used);
		System.gc(); // 참고: 측정 이후 바로 GC 호출은 실측 오차의 원인일 수 있음
	}

	private long getUsedMemory() {
		Runtime runtime = Runtime.getRuntime();
		return runtime.totalMemory() - runtime.freeMemory();
	}
}
