package com.example.taste.domain.match.service;

import static com.example.taste.domain.match.exception.MatchErrorCode.ACTIVE_MATCH_EXISTS;
import static com.example.taste.domain.party.exception.PartyErrorCode.UNAUTHORIZED_PARTY;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.taste.common.exception.CustomException;
import com.example.taste.common.util.EntityFetcher;
import com.example.taste.domain.match.dto.request.PartyMatchCondCreateRequestDto;
import com.example.taste.domain.match.entity.PartyMatchCond;
import com.example.taste.domain.match.entity.UserMatchCond;
import com.example.taste.domain.match.repository.PartyMatchCondRepository;
import com.example.taste.domain.match.repository.UserMatchCondRepository;
import com.example.taste.domain.party.entity.Party;
import com.example.taste.domain.party.enums.InvitationStatus;
import com.example.taste.domain.party.enums.InvitationType;
import com.example.taste.domain.party.enums.MatchStatus;
import com.example.taste.domain.party.repository.PartyInvitationRepository;

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

	@Transactional
	public void cancelUserMatch(Long userMatchCondId) {
		UserMatchCond userMatchCond = entityFetcher.getUserMatchCondOrThrow(userMatchCondId);
		if (userMatchCond.getMatchStatus().equals(MatchStatus.WAITING_HOST)) {
			// 지금 삭제하려는 유저의 매칭으로 생성된 파티 초대이며, 파티 초대 타입이 랜덤, 파티 초대 상태가 WAITING 인 경우
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
}
