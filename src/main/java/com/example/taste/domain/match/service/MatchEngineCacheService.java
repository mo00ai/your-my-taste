package com.example.taste.domain.match.service;

import static com.example.taste.domain.party.exception.PartyErrorCode.PARTY_NOT_FOUND;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.taste.common.exception.CustomException;
import com.example.taste.common.service.RedisService;
import com.example.taste.domain.match.dto.PartyMatchInfoDto;
import com.example.taste.domain.match.entity.AgeRange;
import com.example.taste.domain.match.entity.UserMatchInfo;
import com.example.taste.domain.match.entity.UserMatchInfoCategory;
import com.example.taste.domain.match.repository.UserMatchInfoRepository;
import com.example.taste.domain.party.entity.Party;
import com.example.taste.domain.party.entity.PartyInvitation;
import com.example.taste.domain.party.enums.InvitationStatus;
import com.example.taste.domain.party.enums.InvitationType;
import com.example.taste.domain.party.enums.MatchStatus;
import com.example.taste.domain.party.repository.PartyInvitationRepository;
import com.example.taste.domain.party.repository.PartyRepository;
import com.example.taste.domain.user.enums.Gender;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

@Service
@Slf4j
@RequiredArgsConstructor
public class MatchEngineCacheService {
	private final PartyRepository partyRepository;
	private final UserMatchInfoRepository userMatchInfoRepository;
	private final PartyInvitationRepository partyInvitationRepository;
	private final RedisService redisService;
	private final MeterRegistry meterRegistry;

	// 유저 한 명에게 파티 추천
	// 유저 한 명에게 파티 추천
	// 유저 한 명에게 파티 추천
	@Transactional
	public void runMatchingForUser(List<Long> userMatchInfoIdList) {
		// MEMO : 다 불러와도 되나?
		Timer.Sample sample = Timer.start(meterRegistry);
		try {
			List<UserMatchInfo> matchingUserList =
				userMatchInfoRepository.findAllById(userMatchInfoIdList);
			Set<String> keys = redisService.getKeys("partyMatchInfo*");

			if (keys.isEmpty()) {
				return;
			}

			List<PartyMatchInfoDto> cachedPartyList
				= redisService.getValuesByKeysAsClass(new ArrayList<>(keys), PartyMatchInfoDto.class);

			// 매칭 알고리즘
			List<PartyInvitation> matchedList = new ArrayList<>();

			for (UserMatchInfo matchingUser : matchingUserList) {
				PartyInvitation partyInvitation = runMatchEngine(matchingUser, cachedPartyList);
				if (partyInvitation != null) {
					matchedList.add(partyInvitation);
				}
			}
			partyInvitationRepository.saveAll(matchedList);
		} finally {
			sample.stop(Timer.builder("party.match.cache.execution.time")
				.description("파티 매칭 알고리즘 실행 시간 (ms)")
				.publishPercentileHistogram()
				.register(meterRegistry));
		}
		// TODO : 저장 후 매칭 리스트에 있는 파티장에게 성공 알림 발송 - @윤예진 MEMO : 실패 시 케이스도(매칭된 파티가 없음, 그외 오류) 다뤄야 할까?
	}

	// 파티를 기준으로 맞는 유저 매칭
	@Transactional
	public void runMatchingForParty() {
		List<UserMatchInfo> matchingUserList = userMatchInfoRepository.findAllByMatchStatus(MatchStatus.MATCHING);
		if (matchingUserList.isEmpty()) {
			return;
		}

		List<String> keys = redisService.getKeys("partyMatchInfo*").stream().toList();
		// 매칭 중인 파티가 없는 경우
		if (keys.isEmpty()) {
			return;
		}

		List<PartyMatchInfoDto> cachedPartyList = keys.stream()
			.map(k -> (PartyMatchInfoDto)redisService.getKeyValue(k))
			.filter(Objects::nonNull)
			.toList();

		List<PartyInvitation> matchedList = new ArrayList<>();
		for (UserMatchInfo matchingUser : matchingUserList) {
			PartyInvitation partyInvitation = runMatchEngine(matchingUser, cachedPartyList);
			if (partyInvitation != null) {
				matchedList.add(partyInvitation);
			}
		}
		partyInvitationRepository.saveAll(matchedList);
	}

	private PartyInvitation runMatchEngine(UserMatchInfo matchingUser, List<PartyMatchInfoDto> cachedPartyList) {
		int bestScore = 0;
		List<Long> bestScorePartyIds = new ArrayList<>();

		List<Long> invitedPartyIdList = partyInvitationRepository.findAllPartyIdByUser(matchingUser.getUser().getId());

		List<PartyMatchInfoDto> filteredPartyList = cachedPartyList.stream()
			.filter(p -> !invitedPartyIdList.contains(p.getPartyId()))
			.filter(p -> isMatchStore(matchingUser, p))
			.filter(p -> isMatchRegion(matchingUser, p))
			.filter(p -> isMatchGender(matchingUser, p))
			.filter(p -> isMatchAgeRange(matchingUser, p))
			.toList();

		for (PartyMatchInfoDto party : filteredPartyList) {
			int nowMatchingScore = 0;
			nowMatchingScore += calculateCategoryScore(matchingUser, party);
			nowMatchingScore += calculateFavorScore(matchingUser, party);
			nowMatchingScore += calculateMeetingDateScore(matchingUser, party);

			if (nowMatchingScore != 0 && nowMatchingScore == bestScore) {
				bestScorePartyIds.add(party.getId());
			} else if (nowMatchingScore > bestScore) {
				bestScore = nowMatchingScore;
				bestScorePartyIds.clear();
				bestScorePartyIds.add(party.getId());
			}
		}

		PartyMatchInfoDto selectedParty = null;
		if (bestScore == 0 && !filteredPartyList.isEmpty()) {
			selectedParty = filteredPartyList.get(ThreadLocalRandom.current().nextInt(filteredPartyList.size()));
		} else if (bestScorePartyIds.size() == 1) {
			Long selectedId = bestScorePartyIds.get(0);
			selectedParty = filteredPartyList.stream()
				.filter(p -> p.getId().equals(selectedId))
				.findFirst().orElse(null);
		} else if (bestScorePartyIds.size() > 1) {
			Long selectedId = bestScorePartyIds.get(ThreadLocalRandom.current().nextInt(bestScorePartyIds.size()));
			selectedParty = filteredPartyList.stream()
				.filter(p -> p.getId().equals(selectedId))
				.findFirst().orElse(null);
		}

		if (selectedParty == null)
			return null;
		matchingUser.updateMatchStatus(MatchStatus.WAITING_HOST);
		Party party = partyRepository.findById(selectedParty.getPartyId())
			.orElseThrow(() -> new CustomException(PARTY_NOT_FOUND));

		return new PartyInvitation(
			party, matchingUser.getUser(), matchingUser,
			InvitationType.RANDOM, InvitationStatus.WAITING);
	}

	private boolean isMatchStore(UserMatchInfo matchingUser, PartyMatchInfoDto partyMatchInfo) {
		boolean userHasCondition = matchingUser.getStoreList() != null && !matchingUser.getStoreList().isEmpty();
		boolean partyHasCondition = partyMatchInfo.getStoreId() != null;

		if (!userHasCondition) {
			return true;
		}
		if (userHasCondition && !partyHasCondition) {
			return false;
		}

		return matchingUser.getStoreList().stream()
			.anyMatch(userStore -> userStore.getStore().getId().equals(partyMatchInfo.getStoreId()));
	}

	private boolean isMatchRegion(UserMatchInfo matchingUser, PartyMatchInfoDto partyMatchInfo) {
		boolean userHasCondition = matchingUser.getRegion() != null;
		boolean partyHasCondition = partyMatchInfo.getRegion() != null;

		if (!userHasCondition) {
			return true;
		}
		if (userHasCondition && !partyHasCondition) {
			return false;
		}

		return partyMatchInfo.getRegion().contains(matchingUser.getRegion());
	}

	private boolean isMatchGender(UserMatchInfo matchingUser, PartyMatchInfoDto partyMatchInfo) {
		Gender partyPrefGender = partyMatchInfo.getPrefGender();
		Gender userGender = matchingUser.getUserGender();

		if (partyPrefGender == null || partyPrefGender.equals(Gender.ANY))
			return true;

		return partyPrefGender.equals(userGender);
	}

	private boolean isMatchAgeRange(UserMatchInfo matchingUser, PartyMatchInfoDto partyMatchInfo) {
		int userAge = matchingUser.getUserAge();
		AgeRange userPrefAgeRange = matchingUser.getAgeRange();
		AgeRange partyPrefAgeRange = partyMatchInfo.getAgeRange();

		boolean userHasCondition = userPrefAgeRange != null;
		boolean partyHasCondition = partyPrefAgeRange != null;

		if (!userHasCondition && !partyHasCondition) {
			return true;
		}

		if (!userHasCondition && partyHasCondition) {
			return partyPrefAgeRange.includes(userAge);
		}

		List<PartyInvitation> partyInvitationList = partyInvitationRepository.findByPartyId(partyMatchInfo.getId());
		double avgAge = partyMatchInfo.getAvgAge();

		if (userHasCondition && !partyHasCondition) {
			return userPrefAgeRange.includes(avgAge);
		}

		return partyPrefAgeRange.includes(userAge) && userPrefAgeRange.includes(avgAge);
	}

	// 파티의 음식점 카테고리와 유저의 선호 카테고리들과 겹치는지
	private int calculateCategoryScore(UserMatchInfo matchingUser, PartyMatchInfoDto partyMatchInfo) {
		if (matchingUser.getCategoryList() == null || partyMatchInfo.getStoreCategoryId() == null) {
			return 0;
		}

		if (matchingUser.getCategoryList().stream()
			.map(UserMatchInfoCategory::getCategory)
			.anyMatch(c -> c.getId().equals(partyMatchInfo.getStoreCategoryId()))) {
			return 6;
		}

		return 0;
	}

	// 파티의 선호 입맛과 유저의 선호 입맛이 겹치는지
	private int calculateFavorScore(UserMatchInfo matchingUser, PartyMatchInfoDto partyMatchInfo) {
		if (matchingUser.getFavorList() == null || partyMatchInfo.getFavorList() == null) {
			return 0;
		}
		int score = 0;
		score = matchingUser.getFavorList().stream()
			.map(favor -> favor.getFavor().getName())
			.mapToInt(name ->
				partyMatchInfo.getFavorList().stream()
					.anyMatch(partyFavor -> partyFavor.getName().equals(name)) ? 2 : 0
			)
			.sum();

		return score;
	}

	private int calculateMeetingDateScore(UserMatchInfo matchingUser, PartyMatchInfoDto partyMatchInfo) {
		if (matchingUser.getMeetingDate() == null || partyMatchInfo.getMeetingDate() == null) {
			return 0;
		}

		int dateAbsGap = (int)Math.abs(
			ChronoUnit.DAYS.between(matchingUser.getMeetingDate(), partyMatchInfo.getMeetingDate()));

		switch (dateAbsGap) {
			case 0:
				return 10;
			case 1:
				return 9;
			case 2:
				return 8;
			case 3:
				return 5;
			default:
				if (dateAbsGap < 5) {
					return 2;
				} else {
					return 0;
				}
		}
	}
}
