package com.example.taste.config;

import static com.example.taste.common.constant.CacheConst.*;

import java.util.List;

import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class CacheConfig {
	@Primary
	@Bean(name = "concurrentMapCacheManager")
	public CacheManager cacheManager() {
		SimpleCacheManager cacheManager = new SimpleCacheManager();
		cacheManager.setCaches(List.of(new ConcurrentMapCache(
			TIMEATTACK_CACHE_NAME)));
		return cacheManager;
	}
}