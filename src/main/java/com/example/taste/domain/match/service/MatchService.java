package com.example.taste.domain.match.service;

import static com.example.taste.domain.match.exception.MatchErrorCode.ACTIVE_MATCH_EXISTS;
import static com.example.taste.domain.party.exception.PartyErrorCode.UNAUTHORIZED_PARTY;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.taste.common.exception.CustomException;
import com.example.taste.common.util.EntityFetcher;
import com.example.taste.domain.match.dto.request.PartyMatchCondCreateRequestDto;
import com.example.taste.domain.match.entity.PartyMatchCond;
import com.example.taste.domain.match.entity.UserMatchCond;
import com.example.taste.domain.match.repository.PartyMatchCondRepository;
import com.example.taste.domain.match.repository.UserMatchCondRepository;
import com.example.taste.domain.match.vo.AgeRange;
import com.example.taste.domain.party.entity.Party;
import com.example.taste.domain.party.entity.PartyInvitation;
import com.example.taste.domain.party.enums.InvitationStatus;
import com.example.taste.domain.party.enums.InvitationType;
import com.example.taste.domain.party.enums.MatchStatus;
import com.example.taste.domain.party.repository.PartyInvitationRepository;
import com.example.taste.domain.user.enums.Gender;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchService {
	private final EntityFetcher entityFetcher;
	private final UserMatchCondRepository userMatchCondRepository;
	private final PartyMatchCondRepository partyMatchCondRepository;
	private final PartyInvitationRepository partyInvitationRepository;

	public void registerUserMatch(Long matchConditionId) {
		UserMatchCond userMatchCond = entityFetcher.getUserMatchCondOrThrow(matchConditionId);
		// 이미 매칭 중이라면
		if (userMatchCond.isMatching()) {
			throw new CustomException(ACTIVE_MATCH_EXISTS);
		}
		userMatchCond.registerMatch();
		userMatchCondRepository.save(userMatchCond);
	}

	public void registerPartyMatch(Long hostId, PartyMatchCondCreateRequestDto requestDto) {
		Party party = entityFetcher.getPartyOrThrow(requestDto.getPartyId());

		// 호스트가 아니라면
		if (!party.isHostOfParty(hostId)) {
			throw new CustomException(UNAUTHORIZED_PARTY);
		}

		// 이미 매칭 중이라면
		if (partyMatchCondRepository.existsPartyMatchCondByParty(party)) {
			throw new CustomException(ACTIVE_MATCH_EXISTS);
		}

		partyMatchCondRepository.save(new PartyMatchCond(requestDto, party));
	}

	// 실행 후 1분마다 실행 // MEMO : 유저가 매칭 여러개 동시에 돌리는 경우엔 partyInvitation 정보에 파티 매칭 정보도 적어놔야하나
	@Scheduled(fixedDelay = 60000)
	public void runMatching() {
		// 유저를 기준으로 맞는 파티 매칭
		// MatchingStatus 가 MATCHING 인 유저들만 불러오기
		// 유저에게 파티 초대 상태가 없는 파티인지 확인 필요
		// 유저별로 제일 적합한 파티 하나 추천

		// MEMO : 다 불러와도 되나?
		// MEMO : 있는지 체크하고 그다음에 불러오는 방식 vs (지금) 다 불러오고 체크
		List<UserMatchCond> matchingUserList =
			userMatchCondRepository.findAllByMatchStatus(MatchStatus.MATCHING);
		// 매칭 중인 유저가 없는 경우
		if (matchingUserList.isEmpty()) {
			return;
		}

		List<PartyMatchCond> matchingPartyList = partyMatchCondRepository.findAll();
		// 매칭 중인 파티가 없는 경우
		if (matchingPartyList.isEmpty()) {
			return;
		}

		// 매칭 알고리즘
		List<PartyInvitation> matchedList = new ArrayList<>();

		for (UserMatchCond matchingUser : matchingUserList) {
			int bestScore = 0;
			List<Long> bestScorePartyCondIdList = new ArrayList<>(List.of(0L));

			// 1. 필터링
			Stream<PartyMatchCond> partyStream = matchingPartyList.stream();

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
			List<PartyMatchCond> filteredParty = partyStream.toList();
			for (PartyMatchCond matchingParty : filteredParty) {
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
			PartyMatchCond selectedParty = null;
			if (bestScore == 0 || bestScorePartyCondIdList.size() > 1) {
				selectedParty = filteredParty.get(
					ThreadLocalRandom.current().nextInt(0, filteredParty.size()));
			}
			if (bestScorePartyCondIdList.size() == 1) {
				selectedParty = filteredParty.get(0);
			}
			if (selectedParty == null) {
				log.warn("랜덤 매칭 중 매칭 가능한 파티 찾기에 실패하였습니다. User ID: {}, UserMathCond ID: {}",
					matchingUser.getUser().getId(), matchingUser.getId());
				continue;
			}
			matchingUser.setMatchStatus(MatchStatus.WAITING_HOST);
			matchedList.add(new PartyInvitation(
				selectedParty.getParty(), matchingUser.getUser(), InvitationType.RANDOM, InvitationStatus.WAITING));
		}
	}

	@Transactional
	public void cancelUserMatch(Long userMatchCondId) {
		UserMatchCond userMatchCond = entityFetcher.getUserMatchCondOrThrow(userMatchCondId);
		if (userMatchCond.getMatchStatus().equals(MatchStatus.WAITING_HOST)) {
			// 지금 삭제하려는 매칭으로 생성된 파티 초대이며, 파티 초대 타입이 랜덤, 파티 초대 상태가 WAITING 인 경우
			partyInvitationRepository.deleteUserMatchWhileMatching(
				userMatchCond, InvitationType.RANDOM, InvitationStatus.WAITING);
		}
		userMatchCondRepository.deleteById(userMatchCondId);
	}

	public void cancelPartyMatch(Long hostId, Long partyId) {
		Party party = entityFetcher.getPartyOrThrow(partyId);

		// 호스트가 아니라면
		if (!party.isHostOfParty(hostId)) {
			throw new CustomException(UNAUTHORIZED_PARTY);
		}

		// 수락 대기 중인 파티 초대가 있을 경우
		partyInvitationRepository.deletePartyMatchWhileMatching(
			party, InvitationType.RANDOM, InvitationStatus.WAITING);

		partyMatchCondRepository.deleteByParty(party);
	}

	// 아무 매칭 조건도 설정하지 않았을 때
	private boolean hasNoConditions(UserMatchCond cond) {
		return cond.getMeetingDate() == null
			&& (cond.getCategories() == null || cond.getCategories().isEmpty())
			&& (cond.getRegion() == null || cond.getRegion().isBlank())
			&& cond.getAgeRange() == null;
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

	// 파티의 음식점 카테고리와 유저의 선호 카테고리들과 겹치는지
	private int calculateCategoryScore(UserMatchCond matchingUser, PartyMatchCond party) {
		if (matchingUser.getCategories().contains(party.getStore().getCategory())) {
			return 6;
		}
		return 0;
	}

	private int calculateMeetingDateScore(UserMatchCond matchingUser, PartyMatchCond party) {
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
