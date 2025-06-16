package com.example.taste.domain.chat.service;

import static com.example.taste.domain.party.exception.PartyErrorCode.PARTY_INVITATION_NOT_FOUND;
import static com.example.taste.domain.party.exception.PartyErrorCode.UNAUTHORIZED_PARTY_INVITATION;
import static com.example.taste.domain.user.exception.UserErrorCode.USER_NOT_FOUND;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import com.example.taste.common.exception.CustomException;
import com.example.taste.common.util.EntityFetcher;
import com.example.taste.domain.chat.dto.ChatCreateRequestDto;
import com.example.taste.domain.chat.dto.ChatResponseDto;
import com.example.taste.domain.chat.entity.Chat;
import com.example.taste.domain.chat.repository.ChatRepository;
import com.example.taste.domain.party.entity.Party;
import com.example.taste.domain.party.repository.PartyRepository;
import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class ChatService {
	private final EntityFetcher entityFetcher;
	private final ChatRepository chatRepository;
	private final PartyRepository partyRepository;
	private final UserRepository userRepository;

	// 보낸 메세지 저장
	public ChatResponseDto saveMessage(User user, ChatCreateRequestDto dto) {
		Party party = partyRepository.findByIdWithInvitationsAndUsers(dto.getPartyId())
			.orElseThrow(() -> new CustomException(PARTY_INVITATION_NOT_FOUND));
		if (!party.isActiveMember(user)) {
			throw new CustomException(UNAUTHORIZED_PARTY_INVITATION);
		}
		Chat chat = chatRepository.save(new Chat(dto, user, party));
		// LAZY 로딩 회피용
		User userWithImage = userRepository.findByIdWithImage(user.getId())
			.orElseThrow(() -> new CustomException(USER_NOT_FOUND));
		chat.setUser(userWithImage);
		return new ChatResponseDto(chat);
	}

	public List<ChatResponseDto> getChats(User user, Long partyId) {
		Party party = entityFetcher.getPartyOrThrow(partyId);            // TODO: 특정 파티 가입을 AOP 기반으로 리팩토링 고려 - @윤예진
		if (!party.isActiveMember(user)) {
			throw new CustomException(UNAUTHORIZED_PARTY_INVITATION);
		}
		// TODO: 탈퇴한 사용자 채팅은 어떻게 처리할건지 고민 - @윤예진
		return chatRepository.findAllByPartyIdOrderByCreatedAtAsc(party.getId()).stream()
			.map(ChatResponseDto::new)
			.toList();
	}
}
