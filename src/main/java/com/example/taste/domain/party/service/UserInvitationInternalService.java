package com.example.taste.domain.party.service;

import static com.example.taste.domain.party.enums.InvitationStatus.CONFIRMED;
import static com.example.taste.domain.party.enums.InvitationStatus.WAITING;
import static com.example.taste.domain.party.exception.PartyErrorCode.INVALID_PARTY_INVITATION;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.taste.common.exception.CustomException;
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
import com.example.taste.domain.party.validator.PartyValidator;

@Service
@RequiredArgsConstructor
public class UserInvitationInternalService {
	private final PartyInvitationRepository partyInvitationRepository;
	private final PartyMatchInfoRepository partyMatchInfoRepository;

	// 유저가 랜덤 파티 초대 수락
	@Transactional
	public void confirmRandomPartyInvitation(PartyInvitation partyInvitation) {
		PartyInvitationValidator.IS_WAITING_INVITATION
			.and(PartyInvitationValidator.isInvitationOfParty(partyInvitation.getId()))
			.isSatisfiedBy(partyInvitation);

		Party party = partyInvitation.getParty();
		PartyValidator.IS_AVAILABLE_TO_JOIN_PARTY
			.isSatisfiedBy(party);

		// 매칭 상태가 WAITING_USER 가 아닌 경우
		UserMatchInfo userMatchInfo = partyInvitation.getUserMatchInfo();
		if (!userMatchInfo.isStatus(MatchStatus.WAITING_USER)) {
			throw new CustomException(INVALID_PARTY_INVITATION);
		}

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
	public void confirmInvitedPartyInvitation(PartyInvitation partyInvitation) {
		PartyInvitationValidator.IS_WAITING_INVITATION
			.and(PartyInvitationValidator.isInvitationOfParty(partyInvitation.getId()))
			.isSatisfiedBy(partyInvitation);

		Party party = partyInvitation.getParty();
		PartyValidator.IS_AVAILABLE_TO_JOIN_PARTY
			.isSatisfiedBy(party);

		partyInvitation.updateInvitationStatus(CONFIRMED);
		partyInvitation.getParty().joinMember();

		// 파티가 다 찬 경우 WAITING 상태인 파티 초대들을 삭제
		if (party.isFull()) {
			partyInvitationRepository.deleteAllByPartyAndInvitationStatus(party.getId(), WAITING);
		}

	}

	@Transactional
	public void cancelRequestedPartyInvitation(PartyInvitation partyInvitation) {
		PartyInvitationValidator.IS_WAITING_INVITATION
			.and(PartyInvitationValidator.isInvitationOfParty(partyInvitation.getId()))
			.isSatisfiedBy(partyInvitation);

		partyInvitationRepository.deleteById(partyInvitation.getId());
	}

	// 유저가 랜덤 파티 초대 겨절
	@Transactional
	@MatchEventPublish(matchJobType = MatchJobType.USER_MATCH)
	public List<Long> rejectRandomPartyInvitation(Long userId, PartyInvitation partyInvitation) {
		PartyInvitationValidator.IS_WAITING_INVITATION
			.and(PartyInvitationValidator.isInvitationOfParty(partyInvitation.getId()))
			.isSatisfiedBy(partyInvitation);

		return List.of(userId);
	}

	@Transactional
	public void rejectInvitedPartyInvitation(PartyInvitation partyInvitation) {
		PartyInvitationValidator.IS_WAITING_INVITATION
			.and(PartyInvitationValidator.isInvitationOfParty(partyInvitation.getId()))
			.isSatisfiedBy(partyInvitation);

		Party party = partyInvitation.getParty();
		PartyValidator.IS_AVAILABLE_TO_JOIN_PARTY
			.isSatisfiedBy(party);

		partyInvitation.updateInvitationStatus(InvitationStatus.REJECTED);
	}
}
