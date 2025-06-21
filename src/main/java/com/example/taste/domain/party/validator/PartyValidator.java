package com.example.taste.domain.party.validator;

import static com.example.taste.domain.party.exception.PartyErrorCode.NOT_PARTY_HOST;
import static com.example.taste.domain.party.exception.PartyErrorCode.PARTY_CAPACITY_EXCEEDED;

import com.example.taste.common.condition.Validator;
import com.example.taste.common.exception.CustomException;
import com.example.taste.domain.party.entity.Party;
import com.example.taste.domain.party.enums.PartyStatus;

public final class PartyValidator {
	// 인스턴스화 방지
	private PartyValidator() {
	}

	public static Validator<Party> IS_ACTIVE_PARTY =
		p -> {
			if (p.isStatus(PartyStatus.ACTIVE)) {
				throw new CustomException(PARTY_CAPACITY_EXCEEDED);
			}
			return true;
		};

	public static Validator<Party> IS_FULL_PARTY =
		p -> {
			if (p.isFull()) {
				throw new CustomException(PARTY_CAPACITY_EXCEEDED);
			}
			return true;
		};

	public static final Validator<Party> IS_AVAILABLE_TO_JOIN_PARTY =
		p -> {
			IS_ACTIVE_PARTY.and(IS_FULL_PARTY);
			return true;
		};

	public static Validator<Party> isHostOfParty(Long userId) {
		return p -> {
			if (!p.isHostOfParty(userId)) {
				throw new CustomException(NOT_PARTY_HOST);
			}
			return true;
		};
	}
}
