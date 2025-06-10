package com.example.taste.domain.match.service;

import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.taste.common.util.EntityFetcher;
import com.example.taste.domain.match.dto.request.PartyMatchCondCreateRequestDto;
import com.example.taste.domain.match.entity.PartyMatchCond;
import com.example.taste.domain.match.entity.UserMatchCond;
import com.example.taste.domain.match.repository.PartyMatchCondRepository;
import com.example.taste.domain.match.repository.UserMatchCondRepository;
import com.example.taste.domain.party.entity.Party;
import com.example.taste.domain.party.enums.MatchStatus;

@Service
@RequiredArgsConstructor
public class MatchService {
	private final EntityFetcher entityFetcher;
	private final UserMatchCondRepository userMatchCondRepository;
	private final PartyMatchCondRepository partyMatchCondRepository;

	// 만약에 조건 필드 추가되면 이거 수동으로 조절해줘야하긴하겠지
	private static Map<String, Integer> COND_WEIGHTS = Map.of(
		"STORE", 10,
		"MEETING_TIME", 8,
		"LOCATION", 9,
		"CATEGORY", 7,
		"GENDER", 5,
		"MIN_AGE", 3,
		"MAX_AGE", 3
	);

	@Transactional
	public void registerUserMatch(Long matchConditionId) {
		UserMatchCond userMatchCond = entityFetcher.getUserMatchCondOrThrow(matchConditionId);
		userMatchCond.registerMatch();
	}

	public void registerPartyMatch(PartyMatchCondCreateRequestDto requestDto) {
		Party party = entityFetcher.getPartyOrThrow(requestDto.getPartyId());
		partyMatchCondRepository.save(new PartyMatchCond(requestDto, party));
	}

	// 실행 후 1분마다 실행
	@Scheduled(fixedDelay = 60000)
	public void runMatching() {
		// 유저를 기준으로 맞는 파티 매칭
		// MatchingStatus 가 MATCHING 인 유저들만 불러오기
		// 유저별로 제일 적합한 파티 하나 추천

		List<UserMatchCond> userMatchCondList =
			userMatchCondRepository.findAllByMatchStatus(MatchStatus.MATCHING);

		// for(userMatchCondList : UserMatchCond matchCond) {
		//
		// }
	}
}
