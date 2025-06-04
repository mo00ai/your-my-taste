package com.example.taste.domain.party.service;

import static com.example.taste.domain.party.exception.PartyErrorCode.PARTY_INVITATION_NOT_FOUND;
import static com.example.taste.domain.party.exception.PartyErrorCode.PARTY_NOT_FOUND;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.taste.common.exception.CustomException;
import com.example.taste.domain.party.entity.Party;
import com.example.taste.domain.party.entity.PartyInvitation;
import com.example.taste.domain.party.repository.PartyInvitationRepository;
import com.example.taste.domain.party.repository.PartyRepository;

@Service
@RequiredArgsConstructor
public class PartyInvitationService {
	private final PartyRepository partyRepository;
	private final PartyInvitationRepository partyInvitationRepository;

	@Transactional
	public void leaveParty(Long userId, Long partyId) {
		// 파티 초대 가져와서 상태 변경
		PartyInvitation partyInvitation = partyInvitationRepository.findByUserAndParty(userId, partyId)
			.orElseThrow(() -> new CustomException(PARTY_INVITATION_NOT_FOUND));
		partyInvitation.leave();

		// 파티에서 현재 멤버 수 차감
		Party party = partyRepository.findById(partyId)
			.orElseThrow(() -> new CustomException(PARTY_NOT_FOUND));
		party.leaveMember();
	}
}
