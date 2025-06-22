package com.example.taste.common.interceptor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.example.taste.config.security.CustomUserDetails;
import com.example.taste.domain.party.enums.InvitationStatus;
import com.example.taste.domain.party.repository.PartyInvitationRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {
	// STOMP 통신 중 메시지 전송과 관련한 추가 로직을 처리할 때 사용

	private final String CHAT_SUB_PATTERN = "^/pub/parties/(\\d+)/chat(?:/.*)?$";
	private final PartyInvitationRepository partyInvitationRepository;

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(message);

		// SEND, 자신의 채팅방에 보내는지 검사
		if (headerAccessor.getCommand() == StompCommand.SEND) {
			String destination = headerAccessor.getDestination();
			Pattern pattern = Pattern.compile(CHAT_SUB_PATTERN);
			Matcher matcher = pattern.matcher(destination);
			Authentication auth = (headerAccessor.getUser() instanceof Authentication a) ? a : null;

			try {
				if (auth == null || !auth.isAuthenticated()) {
					throw new IllegalAccessException("인증 정보 없음");
				}
				if (matcher.matches()) {
					Long partyId = Long.valueOf(matcher.group(1));
					CustomUserDetails userDetails = (CustomUserDetails)auth.getPrincipal();

					if (!partyInvitationRepository.existsByUserIdAndPartyIdAndInvitationStatus(
						userDetails.getUser().getId(), partyId, InvitationStatus.CONFIRMED)) {
						throw new IllegalAccessException();
					}
				} else {
					throw new IllegalArgumentException();
				}
			} catch (IllegalArgumentException e) {
				CustomUserDetails userDetails = (CustomUserDetails)auth.getPrincipal();
				log.warn("STOMP 전송 실패: 잘못된 경로 | 유저 ID: {}, 전송 경로: {}", userDetails.getId(), destination);
				return null;
			} catch (IllegalAccessException e) {
				CustomUserDetails userDetails = (CustomUserDetails)auth.getPrincipal();
				log.warn("자신의 활성화된 채팅방이 아니므로 메세지를 보낼 수 없습니다. 유저 ID: {}, 구독 경로: {}", userDetails.getId(), destination);
				return null;
			}
		}

		return message;
	}
}
