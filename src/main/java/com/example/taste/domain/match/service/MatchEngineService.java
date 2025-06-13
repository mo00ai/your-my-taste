package com.example.taste.domain.match.service;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import com.example.taste.common.util.EntityFetcher;
import com.example.taste.domain.match.entity.PartyMatchInfo;
import com.example.taste.domain.match.entity.UserMatchInfo;
import com.example.taste.domain.match.entity.UserMatchInfoCategory;
import com.example.taste.domain.match.entity.UserMatchInfoStore;
import com.example.taste.domain.match.repository.PartyMatchInfoRepository;
import com.example.taste.domain.match.repository.UserMatchInfoRepository;
import com.example.taste.domain.match.vo.AgeRange;
import com.example.taste.domain.party.entity.PartyInvitation;
import com.example.taste.domain.party.enums.InvitationStatus;
import com.example.taste.domain.party.enums.InvitationType;
import com.example.taste.domain.party.enums.MatchStatus;
import com.example.taste.domain.party.repository.PartyInvitationRepository;
import com.example.taste.domain.user.enums.Gender;

@Service
@Slf4j
@RequiredArgsConstructor
public class MatchEngineService {    // 매칭 알고리즘 비동기 실행 워커 서비스
	private final EntityFetcher entityFetcher;
	private final UserMatchInfoRepository userMatchInfoRepository;
	private final PartyMatchInfoRepository partyMatchInfoRepository;
	private final PartyInvitationRepository partyInvitationRepository;

	// 유저 한 명에게 파티 추천
	public void runMatchingForUser(List<Long> userMatchInfoIdList) {
		// MEMO : 다 불러와도 되나?
		// MEMO : 있는지 체크하고 그다음에 불러오는 방식 vs (지금) 다 불러오고 체크
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
		// TODO : 저장 후 매칭 리스트에 있는 파티장에게 성공 알림 발송 - @윤예진 // MEMO : 실패 시 케이스도(매칭된 파티가 없음, 그외 오류) 다뤄야 할까?
	}

	// 파티를 기준으로 맞는 유저 매칭
	public void runMatchingForParty() {
		// MEMO : 다 불러와도 되나?
		// MEMO : 있는지 체크하고 그다음에 불러오는 방식 vs (지금) 다 불러오고 체크
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

	private PartyInvitation runMatchEngine(
		UserMatchInfo matchingUser, List<PartyMatchInfo> matchingPartyList) {
		int bestScore = 0;
		List<Long> bestScorePartyCondIdList = new ArrayList<>(List.of(0L));

		// 1. 필터링
		Stream<PartyMatchInfo> partyStream = matchingPartyList.stream();

		// 초대 이력이 없는 파티들만 필터링
		List<Long> invitedPartyIdList = partyInvitationRepository.findAllPartyIdByUser(matchingUser.getUser());
		partyStream = partyStream.filter(p -> invitedPartyIdList.contains(p.getParty().getId()));

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
		partyStream = matchAgeRange(matchingUser, partyStream);

		// 2. 가중치 계산
		// 카테고리 일치, 날짜 범위
		List<PartyMatchInfo> filteredParty = partyStream.toList();
		for (PartyMatchInfo matchingParty : filteredParty) {
			int nowMatchingScore = 0;
			nowMatchingScore += calculateCategoryScore(matchingUser, matchingParty);
			nowMatchingScore += calculateMeetingDateScore(matchingUser, matchingParty);

			// 동일 최고점 추가 (0이면 무시)
			if (nowMatchingScore != 0 && nowMatchingScore == bestScore) {
				bestScorePartyCondIdList.add(matchingParty.getId());
			}
			// 갱신
			else if (nowMatchingScore > bestScore) {
				bestScore = nowMatchingScore;
				bestScorePartyCondIdList.clear();
				bestScorePartyCondIdList.add(matchingParty.getId());
			}
		}

		// 최종 최고 점수 파티와 매칭
		// 1) 필터링-가중치 계산 이후에도 모든 파티가 0점인 경우, 2)최고 점수인 파티가 여러 개인 경우
		PartyMatchInfo selectedParty = null;
		if (bestScore == 0 || bestScorePartyCondIdList.size() > 1) {
			selectedParty = filteredParty.get(
				ThreadLocalRandom.current().nextInt(0, filteredParty.size()));
		}
		if (bestScorePartyCondIdList.size() == 1) {
			selectedParty = filteredParty.get(0);
		}
		if (selectedParty == null) {
			log.warn("[runMatchingForParty] 랜덤 매칭 중 매칭 가능한 파티 찾기에 실패하였습니다. User ID: {}, UserMathCond ID: {}",
				matchingUser.getUser().getId(), matchingUser.getId());
			return null;
		}
		matchingUser.setMatchStatus(MatchStatus.WAITING_HOST);
		return new PartyInvitation(
			selectedParty.getParty(), matchingUser.getUser(), InvitationType.RANDOM, InvitationStatus.WAITING);
	}

	// 아무 매칭 조건도 설정하지 않았을 때
	private boolean hasNoConditions(UserMatchInfo cond) {
		return cond.getMeetingDate() == null
			&& (cond.getCategories() == null || cond.getCategories().isEmpty())
			&& (cond.getRegion() == null || cond.getRegion().isBlank())
			&& cond.getAgeRange() == null;
	}

	private Stream<PartyMatchInfo> matchStore(UserMatchInfo matchingUser, Stream<PartyMatchInfo> partyStream) {
		return partyStream.filter(p -> p.getStore() != null
			&& matchingUser.getStores().stream()
			.map(UserMatchInfoStore::getStore)
			.toList().contains(p.getStore()));
	}

	private Stream<PartyMatchInfo> matchRegion(UserMatchInfo matchingUser, Stream<PartyMatchInfo> partyStream) {
		return partyStream.filter(p -> p.getRegion() != null && p.getRegion().contains(matchingUser.getRegion()));
	}

	private Stream<PartyMatchInfo> matchGender(UserMatchInfo matchingUser, Stream<PartyMatchInfo> partyStream) {
		return partyStream.filter(p -> {
			if (p.getGender().equals(Gender.ANY)) {
				return true;
			}
			return p.getGender().equals(matchingUser.getUserGender());
		});
	}

	private Stream<PartyMatchInfo> matchAgeRange(UserMatchInfo matchingUser, Stream<PartyMatchInfo> partyStream) {
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

	// 파티의 음식점 카테고리와 유저의 선호 카테고리들과 겹치는지
	private int calculateCategoryScore(UserMatchInfo matchingUser, PartyMatchInfo party) {
		if (matchingUser.getCategories().stream()
			.map(UserMatchInfoCategory::getCategory)
			.toList()
			.contains(party.getStore().getCategory())) {
			return 6;
		}
		return 0;
	}

	private int calculateMeetingDateScore(UserMatchInfo matchingUser, PartyMatchInfo party) {
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
