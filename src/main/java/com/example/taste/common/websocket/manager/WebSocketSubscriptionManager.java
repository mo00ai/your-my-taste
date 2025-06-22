package com.example.taste.common.websocket.manager;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import com.example.taste.config.security.CustomUserDetails;
import com.example.taste.domain.party.enums.InvitationStatus;
import com.example.taste.domain.party.repository.PartyInvitationRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketSubscriptionManager {
	private final String CHAT_SUB_PATTERN = "^/sub/parties/(\\d+)/chat(?:/.*)?$";
	private final PartyInvitationRepository partyInvitationRepository;

	// 구독 상태 관리
	@EventListener
	public void handleChatSubscribeEvent(SessionSubscribeEvent event) {
		try {
			StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
			String destination = headerAccessor.getDestination();
			Pattern pattern = Pattern.compile(CHAT_SUB_PATTERN);
			Matcher matcher = pattern.matcher(destination);

			if (matcher.matches()) {
				Long partyId = Long.valueOf(matcher.group(1));

				Authentication authentication = (Authentication)event.getUser();
				CustomUserDetails userDetails = (CustomUserDetails)authentication.getPrincipal();

				if (!partyInvitationRepository.existsByUserIdAndPartyIdAndInvitationStatus(
					userDetails.getUser().getId(), partyId, InvitationStatus.CONFIRMED)) {
					throw new IllegalAccessException();
				}
			} else {
				throw new IllegalArgumentException();
			}
		} catch (IllegalArgumentException e) {
			StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

			Authentication authentication = (Authentication)event.getUser();
			CustomUserDetails userDetails = (CustomUserDetails)authentication.getPrincipal();

			log.warn("STOMP 구독 실패 유저 ID: {}, 구독 경로: {}", userDetails.getId(), accessor.getDestination());
		} catch (IllegalAccessException e) {
			StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

			Authentication authentication = (Authentication)event.getUser();
			CustomUserDetails userDetails = (CustomUserDetails)authentication.getPrincipal();

			log.warn("자신의 활성화된 채팅방이 아닙니다. 유저 ID: {}, 구독 경로: {}",
				userDetails.getId(), accessor.getDestination());
		}
	}

}
