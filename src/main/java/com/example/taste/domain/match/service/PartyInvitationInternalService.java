package com.example.taste.domain.match.service;

import static com.example.taste.domain.party.enums.InvitationStatus.CONFIRMED;
import static com.example.taste.domain.party.enums.InvitationStatus.WAITING;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.taste.domain.match.annotation.MatchEventPublish;
import com.example.taste.domain.match.enums.MatchJobType;
import com.example.taste.domain.party.entity.Party;
import com.example.taste.domain.party.entity.PartyInvitation;
import com.example.taste.domain.party.enums.InvitationStatus;
import com.example.taste.domain.party.enums.MatchStatus;
import com.example.taste.domain.party.repository.PartyInvitationRepository;
import com.example.taste.domain.party.validator.PartyInvitationValidator;

@Service
@RequiredArgsConstructor
public class PartyInvitationInternalService {
	private final PartyInvitationRepository partyInvitationRepository;

	// 호스트가 랜덤 파티 초대 수락
	@Transactional
	public void confirmRandomPartyInvitation(
		Long hostId, Long partyId, PartyInvitation partyInvitation) {
		Party party = partyInvitation.getParty();

		// 검증
		PartyInvitationValidator.validateAvailableToJoin(party);
		PartyInvitationValidator.validateHostOfParty(party, hostId);
		PartyInvitationValidator.validateUserMatchInfoStatus(
			partyInvitation.getUserMatchInfo(), MatchStatus.WAITING_HOST);

		partyInvitation.getUserMatchInfo().updateMatchStatus(MatchStatus.WAITING_USER);

		// 파티가 다 찬 경우 WAITING 상태인 파티 초대들을 삭제
		if (party.isFull()) {
			partyInvitationRepository.deleteAllByPartyAndInvitationStatus(partyId, WAITING);
		}

	}

	// 호스트가 가입 요청 타입(REQUEST) 수락
	@Transactional
	public void confirmRequestedPartyInvitation(Long hostId, Long partyId, PartyInvitation partyInvitation) {
		Party party = partyInvitation.getParty();

		// 검증
		PartyInvitationValidator.validatePartyOfWaitingInvitation(partyInvitation, party.getId());
		PartyInvitationValidator.validateAvailableToJoin(party);
		PartyInvitationValidator.validateHostOfParty(party, hostId);

		partyInvitation.updateInvitationStatus(CONFIRMED);
		party.joinMember();

		// 파티가 다 찬 경우 WAITING 상태인 파티 초대들을 삭제
		if (party.isFull()) {
			partyInvitationRepository.deleteAllByPartyAndInvitationStatus(partyId, WAITING);
		}

	}

	// 호스트가 파티 초대 취소
	@Transactional
	public void cancelInvitedPartyInvitation(Long hostId, PartyInvitation partyInvitation) {
		Party party = partyInvitation.getParty();

		// 검증
		PartyInvitationValidator.validatePartyOfWaitingInvitation(partyInvitation, party.getId());
		PartyInvitationValidator.validateHostOfParty(party, hostId);

		partyInvitationRepository.deleteById(partyInvitation.getId());
	}

	// 호스트가 파티 가입 요청 거절
	@Transactional
	public void rejectRequestedPartyInvitation(Long hostId, PartyInvitation partyInvitation) {
		Party party = partyInvitation.getParty();

		// 검증
		PartyInvitationValidator.validatePartyOfWaitingInvitation(partyInvitation, party.getId());
		PartyInvitationValidator.validateHostOfParty(party, hostId);

		partyInvitation.updateInvitationStatus(InvitationStatus.REJECTED);
	}

	// 호스트가 랜덤 파티 초대 거절
	@Transactional
	@MatchEventPublish(matchJobType = MatchJobType.USER_MATCH)
	public List<Long> rejectRandomPartyInvitation(
		Long hostId, PartyInvitation partyInvitation) {
		Party party = partyInvitation.getParty();

		// 검증
		PartyInvitationValidator.validatePartyOfWaitingInvitation(partyInvitation, party.getId());
		PartyInvitationValidator.validateHostOfParty(party, hostId);

		partyInvitation.updateInvitationStatus(InvitationStatus.REJECTED);
		return List.of(partyInvitation.getUserMatchInfo().getId());    // 매칭 대상이 될 유저 매칭 조건 ID return
	}
}
