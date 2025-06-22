package com.example.taste.common.websocket.strategy;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.example.taste.common.websocket.manager.WebSocketSubscriptionManager;
import com.example.taste.config.security.CustomUserDetails;
import com.example.taste.domain.party.enums.InvitationStatus;
import com.example.taste.domain.party.repository.PartyInvitationRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscribeCommandStrategy implements StompCommandStrategy {
	private final String CHAT_SUB_PATTERN = "^/sub/parties/(\\d+)/chat(?:/.*)?$";
	private final PartyInvitationRepository partyInvitationRepository;
	private final WebSocketSubscriptionManager subscriptionManager;

	@Override
	public boolean supports(StompCommand command) {
		return StompCommand.SUBSCRIBE.equals(command);
	}

	@Override
	public Message<?> handle(StompHeaderAccessor headerAccessor, Message<?> message) {
		String destination = headerAccessor.getDestination();
		Pattern pattern = Pattern.compile(CHAT_SUB_PATTERN);
		Matcher matcher = pattern.matcher(destination);
		Authentication auth = (headerAccessor.getUser() instanceof Authentication a) ? a : null;
		CustomUserDetails userDetails = (CustomUserDetails)(auth != null ? auth.getPrincipal() : null);

		try {
			if (auth == null || !auth.isAuthenticated()) {
				throw new IllegalAccessException("인증 정보 없음");
			}

			if (matcher.matches()) {
				Long partyId = Long.valueOf(matcher.group(1));

				if (!partyInvitationRepository.existsByUserIdAndPartyIdAndInvitationStatus(
					userDetails != null ?
						userDetails.getUser().getId() : null, partyId, InvitationStatus.CONFIRMED)) {
					throw new IllegalAccessException();
				}
			} else {
				throw new IllegalArgumentException();
			}
		} catch (IllegalArgumentException e) {
			log.warn("STOMP 구독 실패 유저 ID: {}, 구독 경로: {}",
				userDetails != null ? userDetails.getId() : null, headerAccessor.getDestination());
		} catch (IllegalAccessException e) {
			log.warn("자신의 활성화된 채팅방이 아닙니다. 유저 ID: {}, 구독 경로: {}",
				userDetails != null ? userDetails.getId() : null, headerAccessor.getDestination());
		}
		// TODO: 구독 시간 설정 (구독 끊김 이벤트 리스너)
		if (userDetails == null || userDetails.getId() == null) {
			log.warn("인증 정보 없음");
			return null;
		}

		subscriptionManager.add(userDetails.getId(), destination);        // 구독 Map 에 추가
		return message;
	}
}
