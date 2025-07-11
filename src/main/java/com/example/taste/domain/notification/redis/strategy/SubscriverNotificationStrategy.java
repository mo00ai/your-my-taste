package com.example.taste.domain.notification.redis.strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.example.taste.domain.notification.dto.NotificationDataDto;
import com.example.taste.domain.notification.dto.NotificationPublishDto;
import com.example.taste.domain.notification.entity.NotificationContent;
import com.example.taste.domain.notification.entity.enums.NotificationCategory;
import com.example.taste.domain.notification.redis.CategorySupport;
import com.example.taste.domain.notification.service.NotificationService;
import com.example.taste.domain.user.entity.Follow;
import com.example.taste.domain.user.repository.FollowRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriverNotificationStrategy implements NotificationStrategy, CategorySupport {

	private final NotificationService notificationService;
	private final FollowRepository followRepository;

	@Override
	public Set<NotificationCategory> getSupportedCategories() {
		return Set.of(NotificationCategory.SUBSCRIBE);
	}

	@Override
	public void handle(NotificationPublishDto dto) {
		NotificationDataDto dataDto = notificationService.makeDataDto(dto);
		NotificationContent notificationContent = notificationService.saveContent(dataDto);

		// 이 경우 event 가 가진 user id는 게시글을 작성한 유저임
		// 게시글을 작성한 유저를 팔로우 하는 유저를 찾아야 함
		// 우선 구독 관계 테이블에서 해당 유저가 팔로잉 받는 데이터들을 모두 가져옴
		List<Follow> followList = followRepository.findByFollowingId(dto.getUserId());
		// 팔로우 하는 모든 유저를 가져옴
		List<Long> followerIds = new ArrayList<>();
		for (Follow follow : followList) {
			followerIds.add(follow.getFollower().getId());
		}
		notificationService.sendBunchUsingReference(notificationContent, dataDto, followerIds);

	}
}
