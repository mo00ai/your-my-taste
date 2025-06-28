package com.example.taste.domain.match.service;

import static com.example.taste.domain.party.enums.InvitationStatus.CONFIRMED;
import static com.example.taste.domain.party.enums.InvitationStatus.WAITING;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.taste.domain.match.annotation.MatchEventPublish;
import com.example.taste.domain.match.entity.PartyMatchInfo;
import com.example.taste.domain.match.entity.UserMatchInfo;
import com.example.taste.domain.match.enums.MatchJobType;
import com.example.taste.domain.match.repository.PartyMatchInfoRepository;
import com.example.taste.domain.party.entity.Party;
import com.example.taste.domain.party.entity.PartyInvitation;
import com.example.taste.domain.party.enums.InvitationStatus;
import com.example.taste.domain.party.enums.MatchStatus;
import com.example.taste.domain.party.repository.PartyInvitationRepository;
import com.example.taste.domain.party.validator.PartyInvitationValidator;

@Service
@RequiredArgsConstructor
public class UserInvitationInternalService {
	private final PartyInvitationRepository partyInvitationRepository;
	private final PartyMatchInfoRepository partyMatchInfoRepository;

	// 유저가 랜덤 파티 초대 수락
	@Transactional
	public void confirmRandomPartyInvitation(Long userId, PartyInvitation partyInvitation) {
		Party party = partyInvitation.getParty();
		UserMatchInfo userMatchInfo = partyInvitation.getUserMatchInfo();

		// 검증
		PartyInvitationValidator.validateAvailableToJoin(party);
		PartyInvitationValidator.validateUserOfWaitingInvitation(partyInvitation, userId);
		PartyInvitationValidator.validateUserMatchInfoStatus(
			userMatchInfo, MatchStatus.WAITING_USER);

		partyInvitation.updateInvitationStatus(CONFIRMED);
		party.joinMember();
		userMatchInfo.clearMatching();

		// 가입 후 정원이 다 찼다면
		if (party.isFull()) {
			PartyMatchInfo partyMatchInfo =
				partyMatchInfoRepository.findPartyMatchInfoByParty(party);
			partyMatchInfo.updateMatchStatus(MatchStatus.IDLE);
			partyInvitationRepository.deleteAllByPartyAndInvitationStatus(party.getId(), WAITING);
		}
	}

	@Transactional
	public void confirmInvitedPartyInvitation(Long userId, PartyInvitation partyInvitation) {
		Party party = partyInvitation.getParty();

		// 검증
		PartyInvitationValidator.validateAvailableToJoin(party);
		PartyInvitationValidator.validateUserOfWaitingInvitation(partyInvitation, userId);

		partyInvitation.updateInvitationStatus(CONFIRMED);
		partyInvitation.getParty().joinMember();

		// 파티가 다 찬 경우 WAITING 상태인 파티 초대들을 삭제
		if (party.isFull()) {
			partyInvitationRepository.deleteAllByPartyAndInvitationStatus(party.getId(), WAITING);
		}
	}

	@Transactional
	public void cancelRequestedPartyInvitation(Long userId, PartyInvitation partyInvitation) {
		// 검증
		PartyInvitationValidator.validateUserOfWaitingInvitation(partyInvitation, userId);

		partyInvitationRepository.deleteById(partyInvitation.getId());
	}

	// 유저가 랜덤 파티 초대 겨절
	@Transactional
	@MatchEventPublish(matchJobType = MatchJobType.USER_MATCH)
	public List<Long> rejectRandomPartyInvitation(
		Long userId, PartyInvitation partyInvitation) {
		// 검증
		PartyInvitationValidator.validateUserOfWaitingInvitation(partyInvitation, userId);

		return List.of(userId);
	}

	@Transactional
	public void rejectInvitedPartyInvitation(Long userId, PartyInvitation partyInvitation) {
		// 검증
		PartyInvitationValidator.validateUserOfWaitingInvitation(partyInvitation, userId);

		partyInvitation.updateInvitationStatus(InvitationStatus.REJECTED);
	}
}
