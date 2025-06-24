package com.example.taste.common.websocket.manager;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.sun.management.OperatingSystemMXBean;

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

		// Heap Memory 사용량
		Gauge.builder("jvm.memory.used.custom", this, inst -> {
				MemoryUsage heap = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
				return (double)heap.getUsed();
			})
			.baseUnit("bytes")
			.description("Heap Memory Used")
			.register(Metrics.globalRegistry);

		// CPU 사용률
		OperatingSystemMXBean osBean =
			(OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean();

		Gauge.builder("system.cpu.usage", osBean, OperatingSystemMXBean::getSystemCpuLoad)
			.description("System CPU usage (0.0 ~ 1.0)")
			.register(Metrics.globalRegistry);

		Gauge.builder("process.cpu.usage", osBean, OperatingSystemMXBean::getProcessCpuLoad)
			.description("JVM Process CPU usage (0.0 ~ 1.0)")
			.register(Metrics.globalRegistry);
	}

	public void add(Long userId) {
		sessionSet.add(userId);
	}

	public void remove(Long userId) {
		sessionSet.remove(userId);
	}
}
