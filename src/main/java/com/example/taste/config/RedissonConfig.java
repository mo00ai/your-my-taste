package com.example.taste.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

	@Value("${spring.data.redis.host}")
	private String redisHost;

	@Value("${spring.data.redis.port}")
	private int redisPort;

	@Bean(destroyMethod = "shutdown")
	public RedissonClient redissonClient() {
		String address = String.format("redis://%s:%d", redisHost, redisPort);
		Config config = new Config();
		config.useSingleServer()
			.setAddress(address);
		return Redisson.create(config);
	}
}
