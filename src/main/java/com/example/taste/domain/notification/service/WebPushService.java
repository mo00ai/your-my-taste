package com.example.taste.domain.notification.service;

import static com.example.taste.domain.user.exception.UserErrorCode.NOT_FOUND_USER;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.taste.common.exception.CustomException;
import com.example.taste.domain.notification.dto.PushSubscribeRequestDto;
import com.example.taste.domain.notification.entity.WebPushInformation;
import com.example.taste.domain.notification.repository.webPush.WebPushRepository;
import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class WebPushService {
	private final UserRepository userRepository;
	private final WebPushRepository webPushRepository;

	@Transactional
	public void saveSubscription(Long userId, PushSubscribeRequestDto dto) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new CustomException(NOT_FOUND_USER));
		WebPushInformation information = webPushRepository.findByEndpoint(dto.getEndPoint()).orElse(null);

		if (information != null && information.getUser().isSameUser(user.getId())) {
			information.setP256dhKey(dto.getKeys().getP256dh());
			information.setAuthKey(dto.getKeys().getAuth());
			return;
		} else if (information != null) {
			webPushRepository.delete(information);
		}

		information = WebPushInformation.builder()
			.authKey(dto.getKeys().getAuth())
			.endpoint(dto.getEndPoint())
			.p256dhKey(dto.getKeys().getP256dh())
			.user(user)
			.build();
		webPushRepository.save(information);
	}

	public void deleteInformation(Long userId, String endpoint) {
		WebPushInformation information = webPushRepository.getWebPushInformationByUserIdAndEndPoint(userId, endpoint);
		if (information != null) {
			webPushRepository.delete(information);
		}
	}
}
