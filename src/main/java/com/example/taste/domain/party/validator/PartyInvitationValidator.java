package com.example.taste.domain.party.validator;

import static com.example.taste.domain.party.exception.PartyErrorCode.INVALID_PARTY_INVITATION;
import static com.example.taste.domain.party.exception.PartyErrorCode.NOT_ACTIVE_PARTY;
import static com.example.taste.domain.party.exception.PartyErrorCode.NOT_PARTY_HOST;
import static com.example.taste.domain.party.exception.PartyErrorCode.PARTY_CAPACITY_EXCEEDED;
import static com.example.taste.domain.party.exception.PartyErrorCode.UNAUTHORIZED_PARTY_INVITATION;

import com.example.taste.common.exception.CustomException;
import com.example.taste.domain.match.entity.UserMatchInfo;
import com.example.taste.domain.party.entity.Party;
import com.example.taste.domain.party.entity.PartyInvitation;
import com.example.taste.domain.party.enums.InvitationStatus;
import com.example.taste.domain.party.enums.MatchStatus;
import com.example.taste.domain.party.enums.PartyStatus;

public final class PartyInvitationValidator {

	private PartyInvitationValidator() {
	}

	// 파티가 활성 상태인지 검증
	public static void validateActiveParty(Party party) {
		if (!party.isStatus(PartyStatus.ACTIVE)) {
			throw new CustomException(NOT_ACTIVE_PARTY);
		}
	}

	// 파티 정원 초과 여부 검증
	public static void validateNotFullParty(Party party) {
		if (party.isFull()) {
			throw new CustomException(PARTY_CAPACITY_EXCEEDED);
		}
	}

	// 파티 참여 가능 여부 (활성화+정원 미달) 검증
	public static void validateAvailableToJoin(Party party) {
		validateActiveParty(party);
		validateNotFullParty(party);
	}

	// 파티 호스트 여부 검증
	public static void validateHostOfParty(Party party, Long userId) {
		if (!party.isHostOfParty(userId)) {
			throw new CustomException(NOT_PARTY_HOST);
		}
	}

	// 초대 상태가 대기인지 검증
	public static void validateWaitingInvitation(PartyInvitation partyInvitation) {
		if (!partyInvitation.isStatus(InvitationStatus.WAITING)) {
			throw new CustomException(INVALID_PARTY_INVITATION);
		}
	}

	// 초대가 자신의 것인지 검증
	public static void validateInvitationOfUser(PartyInvitation partyInvitation, Long userId) {
		if (!partyInvitation.getUser().getId().equals(userId)) {
			throw new CustomException(UNAUTHORIZED_PARTY_INVITATION);
		}
	}

	// 파티에 소속된 초대인지 검증
	public static void validateInvitationOfParty(PartyInvitation partyInvitation, Long partyId) {
		if (!partyInvitation.getParty().getId().equals(partyId)) {
			throw new CustomException(UNAUTHORIZED_PARTY_INVITATION);
		}
	}

	// 파티에 소속된 초대이며 승인 대기 중인 초대인지 검증
	public static void validatePartyOfWaitingInvitation(
		PartyInvitation partyInvitation, Long partyId) {
		validateWaitingInvitation(partyInvitation);
		validateInvitationOfParty(partyInvitation, partyId);
	}

	// 파티 초대의 소유 유저이며 승인 대기 중인 초대인지 검증
	public static void validateUserOfWaitingInvitation(
		PartyInvitation partyInvitation, Long userId) {
		validateWaitingInvitation(partyInvitation);
		validateInvitationOfUser(partyInvitation, userId);
	}

	// 특정 상태의 유저 매칭 정보인지 검증
	public static void validateUserMatchInfoStatus(
		UserMatchInfo userMatchInfo, MatchStatus matchStatus) {
		if (!userMatchInfo.isStatus(matchStatus)) {
			throw new CustomException(INVALID_PARTY_INVITATION);
		}
	}
}
