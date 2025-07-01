package com.example.taste.domain.match.service;

import static com.example.taste.domain.party.enums.InvitationStatus.CONFIRMED;
import static com.example.taste.domain.party.enums.InvitationStatus.WAITING;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.taste.common.service.RedisService;
import com.example.taste.domain.match.annotation.MatchEventPublish;
import com.example.taste.domain.match.dto.PartyMatchInfoDto;
import com.example.taste.domain.match.entity.UserMatchInfo;
import com.example.taste.domain.match.enums.MatchJobType;
import com.example.taste.domain.match.repository.PartyMatchInfoRepository;
import com.example.taste.domain.party.entity.Party;
import com.example.taste.domain.party.entity.PartyInvitation;
import com.example.taste.domain.party.enums.InvitationStatus;
import com.example.taste.domain.party.enums.MatchStatus;
import com.example.taste.domain.party.repository.PartyInvitationRepository;
import com.example.taste.domain.party.validator.PartyInvitationValidator;
import com.example.taste.domain.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserInvitationInternalService {
	private final RedisService redisService;
	private final UserRepository userRepository;
	private final PartyInvitationRepository partyInvitationRepository;
	private final PartyMatchInfoRepository partyMatchInfoRepository;

	// 유저가 랜덤 파티 초대 수락
	@Transactional
	public void confirmRandomPartyInvitation(Long userId, PartyInvitation partyInvitation) {
		Party party = partyInvitation.getParty();
		UserMatchInfo userMatchInfo = partyInvitation.getUserMatchInfo();

		// 검증
		PartyInvitationValidator.validateAvailableToJoin(party);
		PartyInvitationValidator.validateUserOfWaitingInvitation(partyInvitation, userId);
		PartyInvitationValidator.validateUserMatchInfoStatus(
			userMatchInfo, MatchStatus.WAITING_USER);

		partyInvitation.updateInvitationStatus(CONFIRMED);
		party.joinMember();
		userMatchInfo.clearMatching();

		// 가입 후 정원이 다 찼다면
		if (party.isFull()) {
			partyMatchInfoRepository.findIdByPartyId(party.getId())
				.ifPresent((partyMatchInfoId) -> {
						String key = "partyMatchInfo" + partyMatchInfoId;
						redisService.delete(key);
					}
				);
			partyInvitationRepository.deleteAllByPartyAndInvitationStatus(party.getId(), WAITING);
			return;
		}

		// 랜덤 매칭 상태인 경우 캐시에 평균 나이 업데이트
		updatePartyMatchInfoCacheAfterJoin(party, userMatchInfo.getUserAge());
	}

	@Transactional
	public void confirmInvitedPartyInvitation(Long userId, PartyInvitation partyInvitation) {
		Party party = partyInvitation.getParty();

		// 검증
		PartyInvitationValidator.validateAvailableToJoin(party);
		PartyInvitationValidator.validateUserOfWaitingInvitation(partyInvitation, userId);

		partyInvitation.updateInvitationStatus(CONFIRMED);
		partyInvitation.getParty().joinMember();

		// 파티가 다 찬 경우 WAITING 상태인 파티 초대들을 삭제
		if (party.isFull()) {
			partyInvitationRepository.deleteAllByPartyAndInvitationStatus(party.getId(), WAITING);

			partyMatchInfoRepository.findIdByPartyId(party.getId())
				.ifPresent((partyMatchInfoId) -> {
						String key = "partyMatchInfo" + partyMatchInfoId;
						redisService.delete(key);
					}
				);
			return;
		}

		// 랜덤 매칭 상태인 경우 캐시에 평균 나이 업데이트
		updatePartyMatchInfoCacheAfterJoin(party, partyInvitation.getUser().getAge());
	}

	@Transactional
	public void cancelRequestedPartyInvitation(Long userId, PartyInvitation partyInvitation) {
		// 검증
		PartyInvitationValidator.validateUserOfWaitingInvitation(partyInvitation, userId);

		partyInvitationRepository.deleteById(partyInvitation.getId());
	}

	// 유저가 랜덤 파티 초대 겨절
	@Transactional
	@MatchEventPublish(matchJobType = MatchJobType.USER_MATCH)
	public List<Long> rejectRandomPartyInvitation(
		Long userId, PartyInvitation partyInvitation) {
		// 검증
		PartyInvitationValidator.validateUserOfWaitingInvitation(partyInvitation, userId);

		return List.of(userId);
	}

	@Transactional
	public void rejectInvitedPartyInvitation(Long userId, PartyInvitation partyInvitation) {
		// 검증
		PartyInvitationValidator.validateUserOfWaitingInvitation(partyInvitation, userId);

		partyInvitation.updateInvitationStatus(InvitationStatus.REJECTED);
	}

	private void updatePartyMatchInfoCacheAfterJoin(Party party, int userAge) {
		partyMatchInfoRepository.findIdByPartyId(party.getId())
			.ifPresent((partyMatchInfoId) -> {
				String key = "partyMatchInfo" + partyMatchInfoId;
				PartyMatchInfoDto cachedDto = (PartyMatchInfoDto)redisService.getKeyValue(key);
				if (cachedDto != null) {
					PartyMatchInfoDto newCacheDto = cachedDto.withUpdatedAvgAge(
						cachedDto, party.calculateAvgAgeAfterJoin(cachedDto.getAvgAge(), userAge));
					redisService.setKeyValue(key, newCacheDto);
				}
			});
	}
}
