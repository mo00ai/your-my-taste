package com.example.taste.config;

import java.util.List;

import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class CacheConfig {

	public static final String TIMEATTACK_CACHE_NAME = "timeAttackBoardCache";

	@Bean(name = "concurrentMapCacheManager")
	@Primary
	public CacheManager cacheManager() {
		SimpleCacheManager cacheManager = new SimpleCacheManager();
		cacheManager.setCaches(List.of(new ConcurrentMapCache(
			TIMEATTACK_CACHE_NAME))); // SimpleCacheManager은 중복 허용 안 함 -> list 사용하는 게 순서 보장 측면에서 권장
		return cacheManager;
	}
}