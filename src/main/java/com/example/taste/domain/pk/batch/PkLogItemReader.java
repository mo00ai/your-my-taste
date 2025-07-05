package com.example.taste.domain.pk.batch;

import java.nio.charset.StandardCharsets;

import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStream;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;

public class PkLogItemReader implements ItemReader<String>, ItemStream {

	private final RedisConnection connection;
	private final Cursor<byte[]> cursor;

	public PkLogItemReader(RedisTemplate<String, String> redisTemplate, String pattern) {
		this.connection = redisTemplate.getConnectionFactory().getConnection();
		this.cursor = connection.keyCommands().scan(
			ScanOptions.scanOptions().match(pattern).count(1000).build()
		);
	}

	@Override
	public String read() {
		if (cursor.hasNext()) {
			return new String(cursor.next(), StandardCharsets.UTF_8);
		}
		return null;
	}

	@Override
	public void close() {
		cursor.close();
		connection.close();
	}
}
