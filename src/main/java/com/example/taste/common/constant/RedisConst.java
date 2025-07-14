package com.example.taste.common.constant;

import java.time.Duration;

/**
 * 레디스에서 사용될 상수들 정의해 주세요.
 */
public abstract class RedisConst {
	public final static String DEFAULT = "default";
	public final static String FCFS_KEY_PREFIX = "board:";
	public final static String FCFS_LOCK_KEY_PREFIX = "lock:board:";
	public final static String CACHE_KEY_PREFIX = "cache:board:";
	public static final Duration DEFAULT_TTL = Duration.ofMinutes(10);
}
