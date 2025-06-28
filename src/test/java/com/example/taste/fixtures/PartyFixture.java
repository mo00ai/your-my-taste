package com.example.taste.fixtures;

import java.time.LocalDate;

import com.example.taste.domain.party.entity.Party;
import com.example.taste.domain.party.enums.PartyStatus;
import com.example.taste.domain.user.entity.User;

public class PartyFixture {
	public static Party createWithDate(User user, LocalDate localDate) {
		return Party.oPartyBuilder()
			.hostUser(user)
			.title("테스트 만료 파티 1")
			.description("설명")
			.partyStatus(PartyStatus.ACTIVE)
			.store(null)
			.meetingDate(localDate)
			.maxMembers(5)
			.nowMembers(1)
			.enableRandomMatching(false)
			.buildParty();
	}

	public static Party createExpiredParty(User user, LocalDate localDate) {
		return Party.oPartyBuilder()
			.hostUser(user)
			.title("테스트 삭제 파티 1")
			.description("설명")
			.partyStatus(PartyStatus.EXPIRED)
			.store(null)
			.meetingDate(localDate)
			.maxMembers(5)
			.nowMembers(1)
			.enableRandomMatching(false)
			.buildParty();
	}
}
