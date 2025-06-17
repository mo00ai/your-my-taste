package com.example.taste.domain.chat.service;

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
import com.example.taste.domain.party.entity.PartyInvitation;
import com.example.taste.domain.party.repository.PartyInvitationRepository;
import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class ChatService {
	private final EntityFetcher entityFetcher;
	private final ChatRepository chatRepository;
	private final PartyInvitationRepository partyInvitationRepository;
	private final UserRepository userRepository;

	// 보낸 메세지 저장
	public ChatResponseDto saveMessage(User user, ChatCreateRequestDto dto) {
		Party party = entityFetcher.getPartyOrThrow(dto.getPartyId());
		if (!partyInvitationRepository.isConfirmedPartyMember(dto.getPartyId(), user.getId())) {
			throw new CustomException(UNAUTHORIZED_PARTY_INVITATION);
		}
		Chat chat = chatRepository.save(new Chat(dto, user, party));
		User userWithImage = userRepository.findByIdWithImage(user.getId())
			.orElseThrow(() -> new CustomException(USER_NOT_FOUND));
		chat.setUser(userWithImage);
		return new ChatResponseDto(chat);
	}

	public List<ChatResponseDto> getChats(User user, Long partyId) {
		Party party = entityFetcher.getPartyOrThrow(partyId);
		List<PartyInvitation> partyInvitationList =
			partyInvitationRepository.findByPartyId(partyId);

		if (!partyInvitationRepository.isConfirmedPartyMember(partyId, user.getId())) {
			throw new CustomException(UNAUTHORIZED_PARTY_INVITATION);
		}

		return chatRepository.findAllByPartyIdOrderByCreatedAtAsc(party.getId()).stream()
			.map(ChatResponseDto::new)
			.toList();
	}
}
