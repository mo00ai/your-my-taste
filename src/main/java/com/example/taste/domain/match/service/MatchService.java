package com.example.taste.domain.match.service;

import static com.example.taste.domain.favor.exception.FavorErrorCode.NOT_FOUND_FAVOR;
import static com.example.taste.domain.match.exception.MatchErrorCode.ACTIVE_MATCH_EXISTS;
import static com.example.taste.domain.party.exception.PartyErrorCode.NOT_PARTY_HOST;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.taste.common.exception.CustomException;
import com.example.taste.common.util.EntityFetcher;
import com.example.taste.domain.favor.entity.Favor;
import com.example.taste.domain.favor.repository.FavorRepository;
import com.example.taste.domain.match.annotation.MatchEventPublish;
import com.example.taste.domain.match.dto.request.PartyMatchInfoCreateRequestDto;
import com.example.taste.domain.match.entity.PartyMatchInfo;
import com.example.taste.domain.match.entity.PartyMatchInfoFavor;
import com.example.taste.domain.match.entity.UserMatchInfo;
import com.example.taste.domain.match.enums.MatchJobType;
import com.example.taste.domain.match.repository.PartyMatchInfoRepository;
import com.example.taste.domain.match.repository.UserMatchInfoRepository;
import com.example.taste.domain.party.entity.Party;
import com.example.taste.domain.party.entity.PartyInvitation;
import com.example.taste.domain.party.enums.InvitationStatus;
import com.example.taste.domain.party.enums.InvitationType;
import com.example.taste.domain.party.enums.MatchStatus;
import com.example.taste.domain.party.repository.PartyInvitationRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchService {
	private final EntityFetcher entityFetcher;
	private final FavorRepository favorRepository;
	private final UserMatchInfoRepository userMatchInfoRepository;
	private final PartyMatchInfoRepository partyMatchInfoRepository;
	private final PartyInvitationRepository partyInvitationRepository;

	@Transactional
	@MatchEventPublish(matchJobType = MatchJobType.USER_MATCH)
	public List<Long> registerUserMatch(Long userMatchInfoId) {
		UserMatchInfo userMatchInfo = entityFetcher.getUserMatchInfoOrThrow(userMatchInfoId);
		// 이미 매칭 중이라면
		if (userMatchInfo.isMatching()) {
			throw new CustomException(ACTIVE_MATCH_EXISTS);
		}
		userMatchInfo.registerMatch();
		userMatchInfoRepository.save(userMatchInfo);

		return List.of(userMatchInfo.getId());    // 매칭 대상이 될 유저 매칭 조건 ID
	}

	@Transactional
	@MatchEventPublish(matchJobType = MatchJobType.PARTY_MATCH)
	public void registerPartyMatch(Long hostId, PartyMatchInfoCreateRequestDto requestDto) {
		Party party = entityFetcher.getPartyOrThrow(requestDto.getPartyId());

		// 호스트가 아니라면
		if (!party.isHostOfParty(hostId)) {
			throw new CustomException(NOT_PARTY_HOST);
		}

		// 이미 매칭 중이라면
		if (partyMatchInfoRepository.existsPartyMatchInfoByParty(party)) {
			throw new CustomException(ACTIVE_MATCH_EXISTS);
		}

		PartyMatchInfo partyMatchInfo = partyMatchInfoRepository.save(new PartyMatchInfo(requestDto, party));

		// 입맛 리스트 세팅
		if (requestDto.getFavorList() != null) {
			partyMatchInfo.updateFavorList(getValidPartyMatchInfoFavors(requestDto.getFavorList(), partyMatchInfo));
		}
	}

	@Transactional
	public void cancelUserMatch(Long userMatchInfoId) {
		UserMatchInfo userMatchInfo = entityFetcher.getUserMatchInfoOrThrow(userMatchInfoId);
		if (userMatchInfo.getMatchStatus().equals(MatchStatus.WAITING_HOST)) {
			// 지금 삭제하려는 유저의 매칭으로 생성된 파티 초대이며, 파티 초대 타입이 랜덤, 파티 초대 상태가 WAITING 인 경우
			partyInvitationRepository.deleteUserMatchByTypeAndStatus(
				userMatchInfo.getId(), InvitationType.RANDOM, InvitationStatus.WAITING);
		}
		userMatchInfo.clearMatching();
	}

	@Transactional
	@MatchEventPublish(matchJobType = MatchJobType.USER_MATCH)
	public List<Long> cancelPartyMatch(Long hostId, Long partyId) {
		Party party = entityFetcher.getPartyOrThrow(partyId);

		// 호스트가 아니라면
		if (!party.isHostOfParty(hostId)) {
			throw new CustomException(NOT_PARTY_HOST);
		}

		// 유저 수락 받지 않은(파티장 수락 대기, 수락한) 파티 초대가 있을 경우
		List<PartyInvitation> pendingInvitationList =
			partyInvitationRepository.findByPartyAndInvitationTypeAndStatus(
				party.getId(), InvitationType.RANDOM, InvitationStatus.WAITING);

		partyInvitationRepository.deleteAll(pendingInvitationList);        // 초대 정보 삭제
		partyMatchInfoRepository.deleteByParty(party);            // 파티 매칭 삭제

		return pendingInvitationList.stream()     // 매칭 대상이 될 유저 매칭 조건 ID
			.map(pi -> pi.getUserMatchInfo().getId())
			.toList();
	}

	private List<PartyMatchInfoFavor> getValidPartyMatchInfoFavors(
		List<String> favorNameList, PartyMatchInfo matchInfo) {
		List<Favor> favorList = favorRepository.findAllByNameIn(favorNameList);

		if (favorList.size() != favorNameList.size()) {
			throw new CustomException(NOT_FOUND_FAVOR);
		}

		return favorList.stream()
			.map((f) -> new PartyMatchInfoFavor(matchInfo, f)).toList();
	}
}
