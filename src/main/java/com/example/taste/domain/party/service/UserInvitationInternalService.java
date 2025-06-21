package com.example.taste.domain.party.service;

import static com.example.taste.domain.party.enums.InvitationStatus.CONFIRMED;
import static com.example.taste.domain.party.enums.InvitationStatus.WAITING;
import static com.example.taste.domain.party.exception.PartyErrorCode.INVALID_PARTY_INVITATION;
import static com.example.taste.domain.party.exception.PartyErrorCode.NOT_ACTIVE_PARTY;
import static com.example.taste.domain.party.exception.PartyErrorCode.PARTY_CAPACITY_EXCEEDED;
import static com.example.taste.domain.party.exception.PartyErrorCode.UNAUTHORIZED_PARTY_INVITATION;

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
import com.example.taste.domain.party.enums.PartyStatus;
import com.example.taste.domain.party.repository.PartyInvitationRepository;

@Service
@RequiredArgsConstructor
public class UserInvitationInternalService {
	private final PartyInvitationRepository partyInvitationRepository;
	private final PartyMatchInfoRepository partyMatchInfoRepository;

	// 유저가 랜덤 파티 초대 수락
	@Transactional
	public void confirmRandomPartyInvitation(Long userId, PartyInvitation partyInvitation) {
		// 초대 스테이터스가 대기가 아닌 경우
		validateWaitingInvitationType(partyInvitation.getInvitationStatus());

		// 내 초대가 아닌 경우
		validOwnerOfPartyInvitation(partyInvitation, userId);

		// 파티 모집 중이 아닌 경우
		Party party = partyInvitation.getParty();
		validateActiveAndNotFullParty(party);

		// 매칭 상태가 WAITING_USER 가 아닌 경우
		UserMatchInfo userMatchInfo = partyInvitation.getUserMatchInfo();
		if (!userMatchInfo.isStatus(MatchStatus.WAITING_USER)) {
			throw new CustomException(INVALID_PARTY_INVITATION);
		}

		if (!party.isFull()) {
			partyInvitation.updateInvitationStatus(CONFIRMED);
			party.joinMember();
			userMatchInfo.clearMatching();

			if (party.isFull()) {
				PartyMatchInfo partyMatchInfo =
					partyMatchInfoRepository.findPartyMatchInfoByParty(party);
				partyMatchInfo.updateMatchStatus(MatchStatus.IDLE);
				partyInvitationRepository.deleteAllByPartyAndInvitationStatus(party.getId(), WAITING);
			}
		} else {
			throw new CustomException(PARTY_CAPACITY_EXCEEDED);
		}
	}

	@Transactional
	public void confirmInvitedPartyInvitation(Long userId, PartyInvitation partyInvitation) {
		// 초대 스테이터스가 대기가 아닌 경우
		validateWaitingInvitationType(partyInvitation.getInvitationStatus());

		// 내 초대가 아닌 경우
		validOwnerOfPartyInvitation(partyInvitation, userId);

		// 파티 모집 중이 아닌 경우
		Party party = partyInvitation.getParty();
		validateActiveAndNotFullParty(party);

		if (!party.isFull()) {
			partyInvitation.updateInvitationStatus(CONFIRMED);
			partyInvitation.getParty().joinMember();

			// 파티가 다 찬 경우 WAITING 상태인 파티 초대들을 삭제
			if (party.isFull()) {
				partyInvitationRepository.deleteAllByPartyAndInvitationStatus(party.getId(), WAITING);
			}
		} else {
			throw new CustomException(PARTY_CAPACITY_EXCEEDED);
		}
	}

	@Transactional
	public void cancelRequestedPartyInvitation(Long userId, PartyInvitation partyInvitation) {
		// 초대 스테이터스가 대기가 아닌 경우
		validateWaitingInvitationType(partyInvitation.getInvitationStatus());

		// 내 초대가 아닌 경우
		validOwnerOfPartyInvitation(partyInvitation, userId);

		partyInvitationRepository.deleteById(partyInvitation.getId());
	}

	// 유저가 랜덤 파티 초대 겨절
	@Transactional
	@MatchEventPublish(matchJobType = MatchJobType.USER_MATCH)
	public List<Long> rejectRandomPartyInvitation(Long userId, PartyInvitation partyInvitation) {
		// 초대 스테이터스가 대기가 아닌 경우
		validateWaitingInvitationType(partyInvitation.getInvitationStatus());

		// 내 초대가 아닌 경우
		validOwnerOfPartyInvitation(partyInvitation, userId);
		partyInvitation.updateInvitationStatus(InvitationStatus.REJECTED);

		return List.of(userId);
	}

	@Transactional
	public void rejectInvitedPartyInvitation(Long userId, PartyInvitation partyInvitation) {
		// 초대 스테이터스가 대기가 아닌 경우
		validateWaitingInvitationType(partyInvitation.getInvitationStatus());

		// 내 초대가 아닌 경우
		validOwnerOfPartyInvitation(partyInvitation, userId);

		// 파티 모집 중이 아닌 경우
		Party party = partyInvitation.getParty();
		validateActiveAndNotFullParty(party);

		partyInvitation.updateInvitationStatus(InvitationStatus.REJECTED);
	}

	private void validateWaitingInvitationType(InvitationStatus status) {
		if (!status.equals(WAITING)) {
			throw new CustomException(INVALID_PARTY_INVITATION);
		}
	}

	private void validateActiveAndNotFullParty(Party party) {
		if (!party.isStatus(PartyStatus.ACTIVE)) {
			throw new CustomException(NOT_ACTIVE_PARTY);
		}

		if (party.isFull()) {
			throw new CustomException(PARTY_CAPACITY_EXCEEDED);
		}
	}

	private void validOwnerOfPartyInvitation(PartyInvitation partyInvitation, Long userId) {
		if (!partyInvitation.getUser().getId().equals(userId)) {
			throw new CustomException(UNAUTHORIZED_PARTY_INVITATION);
		}
	}
}
