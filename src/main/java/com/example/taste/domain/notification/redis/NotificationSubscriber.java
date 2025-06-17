package com.example.taste.domain.notification.redis;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.stereotype.Component;

import com.example.taste.common.exception.CustomException;
import com.example.taste.domain.notification.dto.NotificationEventDto;
import com.example.taste.domain.notification.entity.NotificationContent;
import com.example.taste.domain.notification.repository.NotificationContentRepository;
import com.example.taste.domain.notification.service.NotificationService;
import com.example.taste.domain.user.entity.Follow;
import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.exception.UserErrorCode;
import com.example.taste.domain.user.repository.FollowRepository;
import com.example.taste.domain.user.repository.UserRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationSubscriber implements MessageListener {
	private final NotificationService notificationService;
	private final UserRepository userRepository;
	private final FollowRepository followRepository;
	private final NotificationContentRepository contentRepository;
	private final GenericJackson2JsonRedisSerializer serializer;

	// 알림 발행시 자동으로 실행
	@Override
	public void onMessage(Message message, byte[] pattern) {
		// 받은 메시지를 json 스타일로 맵핑
		String json = new String(message.getBody(), StandardCharsets.UTF_8);
		// json 을 역직렬화 하여 dto 로 맵핑
		// redis 은 커스텀 objectMapper 를 사용해 직렬화 하였으므로 역직렬화때도 같은 objectMapper 를 써야 함
		NotificationEventDto event = serializer.deserialize(message.getBody(), NotificationEventDto.class);
		// 받은 알림의 카테고리에 따라 다른 동작
		switch (event.getCategory()) {
			case INDIVIDUAL -> {
				sendIndividual(event);
			}
			case SYSTEM, MARKETING -> {
				sendSystem(event);
			}
			case SUBSCRIBERS -> {
				sendSubscriber(event);
			}
		}
	}

	// 개인에게 보내는 알림
	private void sendIndividual(NotificationEventDto event) {
		NotificationContent content = saveContent(event);
		User user = userRepository.findById(event.getUserId())
			.orElseThrow(() -> new CustomException(UserErrorCode.NOT_FOUND_USER));
		notificationService.sendIndividual(content, event, user);
	}

	// 시스템 알림. 모든 유저에게 전송.
	@SuppressWarnings("checkstyle:RegexpMultiline")
	private void sendSystem(NotificationEventDto event) {
		NotificationContent content = saveContent(event);
		// 페이징 방식
		long startLogging = System.currentTimeMillis();
		// 유저를 100명 단위로 끊어와 보냄
		int page = 0;
		int size = 100;
		Page<User> users;
		do {
			users = userRepository.findAll(PageRequest.of(page, size, Sort.by("id").ascending()));
			notificationService.sendBunch(content, event, users.getContent());
			page++;
		} while (users.hasNext());
		long endLogging = System.currentTimeMillis();
		log.info("paging 타임 체크: {} ms", (endLogging - startLogging));

		/*
		// reference by id
		startLogging = System.currentTimeMillis();
		List<Long> userIds = userRepository.findAllUserId();
		notificationService.sendBunchUsingReference(event, userIds);
		endLogging = System.currentTimeMillis();
		log.info("reference by id 타임 체크", (endLogging - startLogging));
		 */

		/**
		 * * @deprecated ID만 필요할 때는 JPA 프록시 참조를 얻기 위해
		 *   entityManager.getReference(User.class, id) 또는
		 *   userRepository.getReferenceById(id) 사용을 권장합니다.
		 */

		// TODO 두 방식 걸리는 시간 비교할 것.
	}

	// 구독자 알림
	private void sendSubscriber(NotificationEventDto event) {
		NotificationContent content = saveContent(event);
		// 이 경우 event 가 가진 user id는 게시글을 작성한 유저임
		// 게시글을 작성한 유저를 팔로우 하는 유저를 찾아야 함
		// 우선 구독 관계 테이블에서 해당 유저가 팔로잉 받는 데이터들을 모두 가져옴
		List<Follow> followList = followRepository.findByFollowingId(event.getUserId());
		// 팔로우 하는 모든 유저를 가져옴
		List<User> followers = new ArrayList<>();
		for (Follow follow : followList) {
			followers.add(follow.getFollower());
		}
		notificationService.sendBunch(content, event, followers);

		// TODO 위의 시스템 알림에서 두 방식을 비교한 뒤 더 합리적인 방식을 여기에도 적용
	}

	public NotificationContent saveContent(NotificationEventDto event) {
		return contentRepository.save(NotificationContent.builder()
			.content(event.getContent())
			.redirectionUrl(event.getRedirectUrl())
			.build());
	}
}
