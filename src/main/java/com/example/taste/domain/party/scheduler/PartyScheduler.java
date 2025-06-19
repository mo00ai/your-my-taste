package com.example.taste.domain.party.scheduler;

import java.time.LocalDate;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.taste.domain.party.entity.Party;
import com.example.taste.domain.party.enums.PartyStatus;
import com.example.taste.domain.party.repository.PartyRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class PartyScheduler {
	private final PartyRepository partyRepository;

	// 모임일로부터 1~6일 뒤라면 EXPIRED, 모임일로부터 7일 뒤라면 소프트 딜리트 / 매 자정마다 실행
	@Transactional
	@Scheduled(cron = "0 0 0 * * ?")
	public void updateExpiredParties() {
		List<Party> expiredPartyList
			= partyRepository.findAllMeetingDateAndDeleteAtIsNull(LocalDate.now().minusDays(1));
		for (Party party : expiredPartyList) {
			party.updatePartyStatus(PartyStatus.EXPIRED);
		}

		List<Party> deletedPartyList
			= partyRepository.findAllMeetingDateAndDeleteAtIsNull(LocalDate.now().minusDays(7));
		for (Party party : deletedPartyList) {
			party.softDelete();
		}
	}
}
