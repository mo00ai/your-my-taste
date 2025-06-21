package com.example.taste.domain.party.validator;

import static com.example.taste.domain.party.enums.InvitationStatus.WAITING;
import static com.example.taste.domain.party.exception.PartyErrorCode.INVALID_PARTY_INVITATION;
import static com.example.taste.domain.party.exception.PartyErrorCode.UNAUTHORIZED_PARTY_INVITATION;

import com.example.taste.common.condition.Validator;
import com.example.taste.common.exception.CustomException;
import com.example.taste.domain.party.entity.PartyInvitation;

public class PartyInvitationValidator {
	// 인스턴스화 방지
	private PartyInvitationValidator() {
	}

	public static final Validator<PartyInvitation> IS_WAITING_INVITATION =
		pi -> {
			if (!pi.isStatus(WAITING)) {
				throw new CustomException(INVALID_PARTY_INVITATION);
			}
			return true;
		};

	public static Validator<PartyInvitation> isInvitationOfParty(Long partyId) {
		return pi -> {
			if (!pi.getParty().getId().equals(partyId)) {
				throw new CustomException(UNAUTHORIZED_PARTY_INVITATION);
			}
			return true;
		};
	}
}
