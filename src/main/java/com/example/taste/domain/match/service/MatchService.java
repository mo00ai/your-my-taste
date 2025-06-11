package com.example.taste.domain.match.service;

import java.util.List;
import java.util.stream.Stream;

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
import com.example.taste.domain.match.vo.AgeRange;
import com.example.taste.domain.party.entity.Party;
import com.example.taste.domain.party.enums.MatchStatus;
import com.example.taste.domain.user.enums.Gender;

@Service
@RequiredArgsConstructor
public class MatchService {
	private final EntityFetcher entityFetcher;
	private final UserMatchCondRepository userMatchCondRepository;
	private final PartyMatchCondRepository partyMatchCondRepository;

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
		// 유저에게 파티 초대 상태가 없는 파티인지 확인 필요
		// 유저별로 제일 적합한 파티 하나 추천

		// MEMO : 다 불러와도 되나?
		List<UserMatchCond> matchingUserList =
			userMatchCondRepository.findAllByMatchStatus(MatchStatus.MATCHING);

		List<PartyMatchCond> matchingPartyList =
			partyMatchCondRepository.findAll();

		for (UserMatchCond matchingUser : matchingUserList) {
			int maxRating = 0;
			Long maxRatingPartyId = 0L;

			// 필터링 후 가중치 계산
			Stream<PartyMatchCond> partyStream = matchingPartyList.stream();

			// TODO: 초대 이력 없는지 핕터링

			// 가게 필터링
			if (matchingUser.getStores() != null && matchingUser.getStores().isEmpty()) {
				partyStream = matchStore(matchingUser, partyStream);
			}

			// 위치 필터링
			if (matchingUser.getRegion() != null) {
				partyStream = matchRegion(matchingUser, partyStream);
			}

			// 성별 필터링
			if (matchingUser.getUserGender() != null) {
				partyStream = matchGender(matchingUser, partyStream);
			}

			// 나이 필터링
			partyStream = matchRegion(matchingUser, partyStream);

			List<PartyMatchCond> filteredParty = partyStream.toList();

			for (PartyMatchCond matchingParty : filteredParty) {

			}
		}
	}

	private Stream<PartyMatchCond> matchStore(UserMatchCond matchingUser, Stream<PartyMatchCond> partyStream) {
		return partyStream.filter(p -> p.getStore() != null && matchingUser.getStores().contains(p.getStore()));
	}

	private Stream<PartyMatchCond> matchRegion(UserMatchCond matchingUser, Stream<PartyMatchCond> partyStream) {
		return partyStream.filter(p -> p.getRegion() != null && p.getRegion().contains(matchingUser.getRegion()));
	}

	private Stream<PartyMatchCond> matchGender(UserMatchCond matchingUser, Stream<PartyMatchCond> partyStream) {
		return partyStream.filter(p -> {
			if (p.getGender().equals(Gender.ANY)) {
				return true;
			}
			return p.getGender().equals(matchingUser.getUserGender());
		});
	}

	private Stream<PartyMatchCond> matchAgeRange(UserMatchCond matchingUser, Stream<PartyMatchCond> partyStream) {
		int userAge = matchingUser.getUserAge();
		AgeRange userPrefAgeRange = matchingUser.getAgeRange();

		return partyStream.filter(p -> {
			AgeRange partyPrefAgeRange = p.getAgeRange();

			boolean isUserFitPartyCond = true;
			boolean isPartyFitUserCond = true;

			// 파티의 나이 선호 조건이 있다면 --> 유저의 나이가 해당하는 지 체크
			if (partyPrefAgeRange != null) {
				isUserFitPartyCond = partyPrefAgeRange.includes(userAge);
			}

			// 유저의 나이 선호 조건이 있다면 --> 파티 구성원 평균 나이가 해당하는지 체크
			if (userPrefAgeRange != null) {
				isPartyFitUserCond = userPrefAgeRange.includes(p.getParty().calculateAverageMemberAge());
			}

			return isUserFitPartyCond && isPartyFitUserCond;
		});
	}

	// 파티의 음식점 카테고리와 유저의 선호 카테고리 겹치는 것 선택 후 최대 3개까지 반영
	private int calculateCategoryScore(UserMatchCond matchingUser, PartyMatchCond party) {
		int commonCount = (int)matchingUser.getCategories().stream()
			.filter(party.getStore().getCategory()::contains)
			.count();
		return Math.min(commonCount, 3) * 5;
	}

	private int calculateDateScore(UserMatchCond matchingUser, PartyMatchCond party) {
		if (matchingUser.getMeetingTime().equals(party.getDate()))
			return 10;
		return 0;
	}
}
