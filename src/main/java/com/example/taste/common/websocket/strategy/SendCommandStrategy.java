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

import com.example.taste.config.security.CustomUserDetails;
import com.example.taste.domain.party.enums.InvitationStatus;
import com.example.taste.domain.party.repository.PartyInvitationRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class SendCommandStrategy implements StompCommandStrategy {
	private final String CHAT_PUB_PATTERN = "^/pub/parties/(\\d+)/chat(?:/.*)?$";
	private final PartyInvitationRepository partyInvitationRepository;

	@Override
	public boolean supports(StompCommand command) {
		return StompCommand.SEND.equals(command);
	}

	@Override
	public Message<?> handle(StompHeaderAccessor headerAccessor, Message<?> message) {
		// SEND, 자신의 채팅방에 보내는지 검사
		String destination = headerAccessor.getDestination();
		Pattern pattern = Pattern.compile(CHAT_PUB_PATTERN);
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
					userDetails.getUser().getId(), partyId, InvitationStatus.CONFIRMED)) {
					throw new IllegalAccessException();
				}
			} else {
				throw new IllegalArgumentException();
			}
		} catch (IllegalArgumentException e) {
			log.warn("STOMP 전송 실패: 잘못된 경로 | 유저 ID: {}, 전송 경로: {}",
				userDetails != null ? userDetails.getId() : null, destination);
			return null;
		} catch (IllegalAccessException e) {
			log.warn("자신의 활성화된 채팅방이 아니므로 메세지를 보낼 수 없습니다. 유저 ID: {}, 구독 경로: {}",
				userDetails != null ? userDetails.getId() : null, destination);
			return null;
		}

		return message;

	}
}
