package com.example.taste.common.websocket.strategy;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.example.taste.common.websocket.manager.OpenRunPerformanceManager;
import com.example.taste.common.websocket.manager.WebSocketSubscriptionManager;
import com.example.taste.domain.party.enums.InvitationStatus;
import com.example.taste.domain.party.repository.PartyInvitationRepository;
import com.example.taste.domain.user.entity.CustomUserDetails;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscribeCommandStrategy implements StompCommandStrategy {
	private static final Pattern CHAT_SUB_PATTERN_COMPILED = Pattern.compile("^/sub/parties/(\\d+)/chat(?:/.*)?$");
	private static final Pattern BOARD_SUB_PATTERN_COMPILED = Pattern.compile("^/sub/openrun/board/(\\d+)$");
	private final PartyInvitationRepository partyInvitationRepository;
	private final WebSocketSubscriptionManager subscriptionManager;
	private final OpenRunPerformanceManager openRunPerformanceManager;

	@Override
	public boolean supports(StompCommand command) {
		return StompCommand.SUBSCRIBE.equals(command);
	}

	@Override
	public Message<?> handle(StompHeaderAccessor headerAccessor, Message<?> message) {
		String destination = headerAccessor.getDestination();
		if (destination == null) {
			return null;
		}

		Matcher chatMatcher = CHAT_SUB_PATTERN_COMPILED.matcher(destination);
		Matcher boardMatcher = BOARD_SUB_PATTERN_COMPILED.matcher(destination);
		Authentication auth = (headerAccessor.getUser() instanceof Authentication a) ? a : null;
		CustomUserDetails userDetails = (CustomUserDetails)(auth != null ? auth.getPrincipal() : null);
		try {
			if (auth == null || !auth.isAuthenticated()) {
				throw new IllegalAccessException("인증 정보 없음");
			}
			if (boardMatcher.matches()) {
				subscriptionManager.add(userDetails.getId(), destination);
				openRunPerformanceManager.add(userDetails.getId());
				return message;
			}
			if (chatMatcher.matches()) {
				Long partyId = Long.valueOf(chatMatcher.group(1));

				if (!partyInvitationRepository.existsByUserIdAndPartyIdAndInvitationStatus(
					userDetails != null ?
						userDetails.getId() : null, partyId, InvitationStatus.CONFIRMED)) {
					throw new IllegalAccessException();
				}
			} else {
				throw new IllegalArgumentException();
			}
		} catch (IllegalArgumentException e) {
			log.warn("STOMP 구독 실패 유저 ID: {}, 구독 경로: {}",
				userDetails != null ? userDetails.getId() : null, headerAccessor.getDestination());
			return null;
		} catch (IllegalAccessException e) {
			log.warn("자신의 활성화된 채팅방이 아닙니다. 유저 ID: {}, 구독 경로: {}",
				userDetails != null ? userDetails.getId() : null, headerAccessor.getDestination());
			return null;
		}
		if (userDetails == null || userDetails.getId() == null) {
			log.warn("인증 정보 없음");
			return null;
		}

		subscriptionManager.add(userDetails.getId(), destination);        // 구독 Map 에 추가
		return message;
	}
}
