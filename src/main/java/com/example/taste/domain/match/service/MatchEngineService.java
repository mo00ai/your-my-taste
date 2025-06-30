package com.example.taste.domain.match.service;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.taste.domain.match.entity.AgeRange;
import com.example.taste.domain.match.entity.PartyMatchInfo;
import com.example.taste.domain.match.entity.PartyMatchInfoFavor;
import com.example.taste.domain.match.entity.UserMatchInfo;
import com.example.taste.domain.match.entity.UserMatchInfoCategory;
import com.example.taste.domain.match.repository.PartyMatchInfoRepository;
import com.example.taste.domain.match.repository.UserMatchInfoRepository;
import com.example.taste.domain.party.entity.PartyInvitation;
import com.example.taste.domain.party.enums.InvitationStatus;
import com.example.taste.domain.party.enums.InvitationType;
import com.example.taste.domain.party.enums.MatchStatus;
import com.example.taste.domain.party.repository.PartyInvitationRepository;
import com.example.taste.domain.user.enums.Gender;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

@Service
@Slf4j
@RequiredArgsConstructor
public class MatchEngineService {    // 매칭 알고리즘 비동기 실행 워커 서비스
	private final UserMatchInfoRepository userMatchInfoRepository;
	private final PartyMatchInfoRepository partyMatchInfoRepository;
	private final PartyInvitationRepository partyInvitationRepository;
	private final MeterRegistry meterRegistry;

	// 유저 한 명에게 파티 추천
	@Transactional
	public void runMatchingForUser(List<Long> userMatchInfoIdList) {
		// MEMO : 다 불러와도 되나?
		Timer.Sample sample = Timer.start(meterRegistry);
		try {
			List<UserMatchInfo> matchingUserList =
				userMatchInfoRepository.findAllById(userMatchInfoIdList);

			List<PartyMatchInfo> matchingPartyList = partyMatchInfoRepository.findAll();
			// 매칭 중인 파티가 없는 경우
			if (matchingPartyList.isEmpty()) {
				return;
			}

			// 매칭 알고리즘
			List<PartyInvitation> matchedList = new ArrayList<>();

			for (UserMatchInfo matchingUser : matchingUserList) {
				PartyInvitation partyInvitation = runMatchEngine(matchingUser, matchingPartyList);
				if (partyInvitation != null) {
					matchedList.add(partyInvitation);
				}
			}
			partyInvitationRepository.saveAll(matchedList);
		} finally {
			sample.stop(Timer.builder("party.match.execution.time")
				.description("파티 매칭 알고리즘 실행 시간 (ms)")
				.publishPercentileHistogram()
				.register(meterRegistry));
		}
		// TODO : 저장 후 매칭 리스트에 있는 파티장에게 성공 알림 발송 - @윤예진 MEMO : 실패 시 케이스도(매칭된 파티가 없음, 그외 오류) 다뤄야 할까?
	}

	// 파티를 기준으로 맞는 유저 매칭
	@Transactional
	public void runMatchingForParty() {
		// MEMO : 다 불러와도 되나?
		List<UserMatchInfo> matchingUserList =
			userMatchInfoRepository.findAllByMatchStatus(MatchStatus.MATCHING);

		// 매칭 중인 유저가 없는 경우
		if (matchingUserList.isEmpty()) {
			return;
		}

		List<PartyMatchInfo> matchingPartyList = partyMatchInfoRepository.findAll();
		// 매칭 중인 파티가 없는 경우
		if (matchingPartyList.isEmpty()) {
			return;
		}

		// 매칭 알고리즘
		List<PartyInvitation> matchedList = new ArrayList<>();

		for (UserMatchInfo matchingUser : matchingUserList) {
			PartyInvitation partyInvitation = runMatchEngine(matchingUser, matchingPartyList);
			if (partyInvitation != null) {
				matchedList.add(partyInvitation);
			}
		}
		partyInvitationRepository.saveAll(matchedList);
		// TODO : 저장 후 매칭 리스트에 있는 파티장에게 성공 알림 발송 - @윤예진
	}

	// TODO : 조건 조합 제한 필요 (가게 선택하면 위치, 카테고리, 입맛은 제한하는 식)
	private PartyInvitation runMatchEngine(
		UserMatchInfo matchingUser, List<PartyMatchInfo> matchingPartyList) {
		int bestScore = 0;
		List<Long> bestScorePartyMatchInfoIdList = new ArrayList<>();

		// 1. 필터링
		List<Long> invitedPartyIdList = partyInvitationRepository.findAllPartyIdByUser(matchingUser.getUser().getId());

		List<PartyMatchInfo> filteredPartyList = matchingPartyList.stream()
			.filter(p -> !invitedPartyIdList.contains(p.getParty().getId()))        // 초대 이력 없음
			.filter(p -> isMatchStore(matchingUser, p))                                // 가게 일치 여부
			.filter(p -> isMatchRegion(matchingUser, p))                            // 위치 일치 여부
			.filter(p -> isMatchGender(matchingUser, p))                            // 성별 선호 일치 여부
			.filter(p -> isMatchAgeRange(matchingUser, p))                            // 나이대 선호 일치 여부
			.toList();

		// 2. 가중치 계산
		// 카테고리 일치, 입맛 일치, 날짜 범위
		for (PartyMatchInfo matchingParty : filteredPartyList) {
			int nowMatchingScore = 0;
			nowMatchingScore += calculateCategoryScore(matchingUser, matchingParty);
			nowMatchingScore += calculateFavorScore(matchingUser, matchingParty);
			nowMatchingScore += calculateMeetingDateScore(matchingUser, matchingParty);

			// 동일 최고점 추가 (0이면 무시)
			if (nowMatchingScore != 0 && nowMatchingScore == bestScore) {
				bestScorePartyMatchInfoIdList.add(matchingParty.getId());
			}
			// 갱신
			else if (nowMatchingScore > bestScore) {
				bestScore = nowMatchingScore;
				bestScorePartyMatchInfoIdList.clear();
				bestScorePartyMatchInfoIdList.add(matchingParty.getId());
			}
		}

		// 최종 최고 점수 파티와 매칭
		// 1) 필터링-가중치 계산 이후에도 모든 파티가 0점인 경우, 2)최고 점수인 파티가 여러 개인 경우
		PartyMatchInfo selectedParty = null;
		// 모든 점수가 0인 경우 랜덤 선택
		if (bestScore == 0 && !filteredPartyList.isEmpty()) {
			selectedParty = filteredPartyList.get(
				ThreadLocalRandom.current().nextInt(0, filteredPartyList.size()));
		}
		// 최고 점수 파티가 하나인 경우
		else if (bestScorePartyMatchInfoIdList.size() == 1) {
			Long selectedId = bestScorePartyMatchInfoIdList.get(0);
			selectedParty = filteredPartyList.stream()
				.filter(p -> p.getId().equals(selectedId))
				.findFirst()
				.orElse(null);
		}
		// 최고점 파티가 여러 개인 경우 랜덤 선택
		else if (bestScorePartyMatchInfoIdList.size() > 1) {
			Long selectedId = bestScorePartyMatchInfoIdList.get(
				ThreadLocalRandom.current().nextInt(0, bestScorePartyMatchInfoIdList.size()));
			selectedParty = filteredPartyList.stream()
				.filter(p -> p.getId().equals(selectedId))
				.findFirst()
				.orElse(null);
		}

		if (selectedParty == null) {
			log.warn("[runMatchEngine] 랜덤 매칭 중 매칭 가능한 파티 찾기에 실패하였습니다. User ID: {}, UserMatchInfo ID: {}",
				matchingUser.getUser().getId(), matchingUser.getId());
			return null;
		}
		matchingUser.updateMatchStatus(MatchStatus.WAITING_HOST);
		return new PartyInvitation(
			selectedParty.getParty(), matchingUser.getUser(), matchingUser, InvitationType.RANDOM,
			InvitationStatus.WAITING);
	}

	// 아무 매칭 조건도 설정하지 않았을 때
	private boolean hasNoPreferenceSet(UserMatchInfo info) {
		return info.getMeetingDate() == null
			&& (info.getCategoryList() == null || info.getCategoryList().isEmpty())
			&& (info.getRegion() == null || info.getRegion().isBlank())
			&& info.getAgeRange() == null;
	}

	private boolean isMatchStore(UserMatchInfo matchingUser, PartyMatchInfo partyMatchInfo) {
		boolean userHasCondition = matchingUser.getStoreList() != null && !matchingUser.getStoreList().isEmpty();
		boolean partyHasCondition = partyMatchInfo.getStore() != null;

		if (!userHasCondition) {
			return true;
		}
		if (userHasCondition && !partyHasCondition) {
			return false;
		}

		return matchingUser.getStoreList().stream()
			.anyMatch(userStore -> userStore.getStore().equals(partyMatchInfo.getStore()));
	}

	private boolean isMatchRegion(UserMatchInfo matchingUser, PartyMatchInfo partyMatchInfo) {
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

	private boolean isMatchGender(UserMatchInfo matchingUser, PartyMatchInfo partyMatchInfo) {
		Gender partyPrefGender = partyMatchInfo.getGender();
		Gender userGender = matchingUser.getUserGender();

		if (partyPrefGender == null || partyPrefGender.equals(Gender.ANY))
			return true;

		return partyPrefGender.equals(userGender);
	}

	private boolean isMatchAgeRange(UserMatchInfo matchingUser, PartyMatchInfo partyMatchInfo) {
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
		double avgAge = partyMatchInfo.getParty().calculateAverageMemberAge(partyInvitationList);

		if (userHasCondition && !partyHasCondition) {
			return userPrefAgeRange.includes(avgAge);
		}

		return partyPrefAgeRange.includes(userAge) && userPrefAgeRange.includes(avgAge);
	}

	// 파티의 음식점 카테고리와 유저의 선호 카테고리들과 겹치는지
	private int calculateCategoryScore(UserMatchInfo matchingUser, PartyMatchInfo party) {
		if (matchingUser.getCategoryList() == null || party.getStore().getCategory() == null) {
			return 0;
		}

		if (matchingUser.getCategoryList().stream()
			.map(UserMatchInfoCategory::getCategory)
			.toList()
			.contains(party.getStore().getCategory())) {
			return 6;
		}

		return 0;
	}

	// 파티의 선호 입맛과 유저의 선호 입맛이 겹치는지
	private int calculateFavorScore(UserMatchInfo matchingUser, PartyMatchInfo party) {
		if (matchingUser.getFavorList() == null || party.getFavorList() == null) {
			return 0;
		}
		int score = 0;
		score = matchingUser.getFavorList().stream()
			.map(favor -> favor.getFavor().getName())
			.mapToInt(name ->
				party.getFavorList().stream()
					.map(PartyMatchInfoFavor::getFavor)
					.anyMatch(partyFavor -> partyFavor.getName().equals(name)) ? 2 : 0
			)
			.sum();

		return score;
	}

	private int calculateMeetingDateScore(UserMatchInfo matchingUser, PartyMatchInfo party) {
		if (matchingUser.getMeetingDate() == null || party.getMeetingDate() == null) {
			return 0;
		}

		int dateAbsGap = (int)Math.abs(
			ChronoUnit.DAYS.between(matchingUser.getMeetingDate(), party.getMeetingDate()));

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
