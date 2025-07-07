package com.example.taste.domain.chat.service;

import static com.example.taste.domain.party.exception.PartyErrorCode.PARTY_NOT_FOUND;
import static com.example.taste.domain.party.exception.PartyErrorCode.UNAUTHORIZED_PARTY_INVITATION;
import static com.example.taste.domain.user.exception.UserErrorCode.NOT_FOUND_USER;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import com.example.taste.common.exception.CustomException;
import com.example.taste.domain.chat.dto.ChatCreateRequestDto;
import com.example.taste.domain.chat.dto.ChatResponseDto;
import com.example.taste.domain.chat.entity.Chat;
import com.example.taste.domain.chat.repository.ChatRepository;
import com.example.taste.domain.party.entity.Party;
import com.example.taste.domain.party.repository.PartyInvitationRepository;
import com.example.taste.domain.party.repository.PartyRepository;
import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class ChatService {
	private final ChatRepository chatRepository;
	private final UserRepository userRepository;
	private final PartyRepository partyRepository;
	private final PartyInvitationRepository partyInvitationRepository;

	// 보낸 메세지 저장
	public ChatResponseDto saveMessage(Long userId, ChatCreateRequestDto dto) {
		User user = userRepository.findByIdWithImage(userId)
			.orElseThrow(() -> new CustomException(NOT_FOUND_USER));
		Party party = partyRepository.findById(dto.getPartyId())
			.orElseThrow(() -> new CustomException(PARTY_NOT_FOUND));
		if (!partyInvitationRepository.isConfirmedPartyMember(dto.getPartyId(), userId)) {
			throw new CustomException(UNAUTHORIZED_PARTY_INVITATION);
		}
		Chat chat = chatRepository.save(new Chat(dto, user, party));

		chat.setUser(user);
		return new ChatResponseDto(chat);
	}

	public List<ChatResponseDto> getChats(Long userId, Long partyId) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new CustomException(NOT_FOUND_USER));
		Party party = partyRepository.findById(partyId)
			.orElseThrow(() -> new CustomException(PARTY_NOT_FOUND));
		if (!partyInvitationRepository.isConfirmedPartyMember(partyId, user.getId())) {
			throw new CustomException(UNAUTHORIZED_PARTY_INVITATION);
		}

		return chatRepository.findAllByPartyIdOrderByCreatedAtAsc(party.getId()).stream()
			.map(ChatResponseDto::new)
			.toList();
	}
}
