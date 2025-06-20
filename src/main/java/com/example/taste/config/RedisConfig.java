package com.example.taste.config;

import static com.example.taste.common.constant.RedisConst.DEFAULT;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.ApplicationRunner;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

import com.example.taste.common.constant.RedisChannel;
import com.example.taste.domain.match.redis.MatchSubscriber;
import com.example.taste.domain.notification.redis.NotificationSubscriber;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
@EnableCaching
@EnableRedisHttpSession
public class RedisConfig {

	@Bean
	public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
		RedisTemplate<String, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(connectionFactory);

		// Redis용 ObjectMapper 설정
		ObjectMapper redisObjectMapper = createRedisObjectMapper();

		template.setKeySerializer(new StringRedisSerializer());
		template.setValueSerializer(new GenericJackson2JsonRedisSerializer(redisObjectMapper));
		template.setHashKeySerializer(new StringRedisSerializer());
		template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer(redisObjectMapper));
		template.setEnableTransactionSupport(true);
		template.afterPropertiesSet();

		return template;
	}

	@Bean(name = "redisCacheManager")
	public RedisCacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
		// Redis용 ObjectMapper 설정
		ObjectMapper redisObjectMapper = createRedisObjectMapper();
		GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(redisObjectMapper);

		RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
			.serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
			.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer))
			.disableCachingNullValues();

		// 각 캐시별 TTL 설정
		Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
		cacheConfigurations.put(DEFAULT, defaultConfig.entryTtl(Duration.ofMinutes(10)));// 기본 유효기간 10분
		cacheConfigurations.put("pkCriteriaCache", RedisCacheConfiguration.defaultCacheConfig());
		// Todo 캐싱할 데이터의 key값을 자유롭게 설정해서 입력해 주세요.

		return RedisCacheManager.builder(connectionFactory)
			.cacheDefaults(defaultConfig.entryTtl(Duration.ofHours(1)))
			.withInitialCacheConfigurations(cacheConfigurations)
			.build();
	}

	private ObjectMapper createRedisObjectMapper() {
		ObjectMapper redisObjectMapper = new ObjectMapper();
		redisObjectMapper.registerModule(new JavaTimeModule());

		redisObjectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		// 타입 검증기 설정
		PolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
			.allowIfBaseType(Object.class)
			.build();

		redisObjectMapper.activateDefaultTyping(typeValidator, ObjectMapper.DefaultTyping.NON_FINAL_AND_ENUMS);

		// enum 타입 역직렬화
		redisObjectMapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
		redisObjectMapper.configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, true);

		return redisObjectMapper;
	}

	// redis event 리스너
	@Bean
	public RedisMessageListenerContainer redisMessageListenerContainer(
		RedisConnectionFactory connectionFactory,
		MatchSubscriber matchSubscriber,
		NotificationSubscriber notificationSubscriber) {
		RedisMessageListenerContainer container = new RedisMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);

		// MEMO : 필요한 채널과 리스너를 파라미터와 이곳에 추가해주세요.
		container.addMessageListener(matchSubscriber, new ChannelTopic(RedisChannel.MATCH_CHANNEL));
		container.addMessageListener(notificationSubscriber, new ChannelTopic(RedisChannel.NOTIFICATION_CHANNEL));

		return container;
	}

	//  로컬환경에서 Redis 서버 설정 편의상 스프링부트에서 강제로 설정
	@Bean
	public ApplicationRunner redisNotifyEventConfigurer(RedisConnectionFactory factory) {
		return args -> {
			RedisConnection connection = factory.getConnection();
			String config = (String)connection.getConfig("notify-keyspace-events").get("notify-keyspace-events");

			if (!config.contains("Ex")) {
				connection.setConfig("notify-keyspace-events", config + "Ex");
				System.out.println("[INFO] Redis notify-keyspace-events set to: " + config + "Ex");
			}
		};
	}

	// 역직렬화용으로 사용
	@Bean
	public GenericJackson2JsonRedisSerializer redisSerializer() {
		return new GenericJackson2JsonRedisSerializer(createRedisObjectMapper());
	}

	@Bean
	public RedisConnectionFactory redisConnectionFactory() {
		RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
		return new LettuceConnectionFactory(redisStandaloneConfiguration);
	}
}
