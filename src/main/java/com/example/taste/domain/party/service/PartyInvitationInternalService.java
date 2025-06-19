package com.example.taste.domain.party.service;

import static com.example.taste.domain.party.enums.InvitationStatus.CONFIRMED;
import static com.example.taste.domain.party.enums.InvitationStatus.WAITING;
import static com.example.taste.domain.party.exception.PartyErrorCode.INVALID_PARTY_INVITATION;
import static com.example.taste.domain.party.exception.PartyErrorCode.NOT_PARTY_HOST;
import static com.example.taste.domain.party.exception.PartyErrorCode.NOT_RECRUITING_PARTY;
import static com.example.taste.domain.party.exception.PartyErrorCode.UNAUTHORIZED_PARTY_INVITATION;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.taste.common.exception.CustomException;
import com.example.taste.common.util.EntityFetcher;
import com.example.taste.domain.match.annotation.MatchEventPublish;
import com.example.taste.domain.match.entity.UserMatchInfo;
import com.example.taste.domain.match.enums.MatchJobType;
import com.example.taste.domain.party.entity.Party;
import com.example.taste.domain.party.entity.PartyInvitation;
import com.example.taste.domain.party.enums.InvitationStatus;
import com.example.taste.domain.party.enums.MatchStatus;
import com.example.taste.domain.party.enums.PartyStatus;
import com.example.taste.domain.party.repository.PartyInvitationRepository;

@Service
@RequiredArgsConstructor
public class PartyInvitationInternalService {
	private final EntityFetcher entityFetcher;
	private final PartyInvitationRepository partyInvitationRepository;

	// 호스트가 랜덤 파티 초대 수락
	@Transactional
	public void confirmRandomPartyInvitation(
		Long hostId, Long partyId, PartyInvitation partyInvitation) {
		// 초대 스테이터스가 대기가 아닌 경우
		validateWaitingInvitationType(partyInvitation.getInvitationStatus());
		// 해당 파티의 초대가 아닌 경우
		validInvitationOfParty(partyInvitation, partyId);

		// 파티 모집 중이 아닌 경우
		Party party = entityFetcher.getPartyOrThrow(partyId);
		validateRecruitingParty(party);

		// 호스트가 아닌 경우
		if (!party.isHostOfParty(hostId)) {
			throw new CustomException(NOT_PARTY_HOST);
		}

		// 매칭 상태가 WAITING_HOST 가 아닌 경우
		UserMatchInfo userMatchInfo = partyInvitation.getUserMatchInfo();
		if (!userMatchInfo.isStatus(MatchStatus.WAITING_HOST)) {
			throw new CustomException(INVALID_PARTY_INVITATION);
		}

		if (!party.isFull()) {
			userMatchInfo.updateMatchStatus(MatchStatus.WAITING_USER);

			// 파티가 다 찬 경우 WAITING 상태인 파티 초대들을 삭제
			if (party.isFull()) {
				partyInvitationRepository.deleteAllByPartyAndInvitationStatus(partyId, WAITING);
			}
		} else {
			// 파티가 다 찬 경우 WAITING 상태인 파티 초대들을 삭제
			partyInvitationRepository.deleteAllByPartyAndInvitationStatus(partyId, WAITING);
			throw new CustomException(NOT_RECRUITING_PARTY);
		}
	}

	// 호스트가 가입 요청 타입(REQUEST) 수락
	@Transactional
	public void confirmRequestedPartyInvitation(Long hostId, Long partyId, PartyInvitation partyInvitation) {
		// 초대 스테이터스가 대기가 아닌 경우
		validateWaitingInvitationType(partyInvitation.getInvitationStatus());
		// 해당 파티의 초대가 아닌 경우
		validInvitationOfParty(partyInvitation, partyId);

		Party party = entityFetcher.getPartyOrThrow(partyId);
		// 파티 모집 중이 아닌 경우
		validateRecruitingParty(party);

		// 호스트가 아닌 경우
		if (!party.isHostOfParty(hostId)) {
			throw new CustomException(NOT_PARTY_HOST);
		}

		if (!party.isFull()) {
			partyInvitation.updateInvitationStatus(CONFIRMED);
			partyInvitation.getParty().joinMember();
			// 파티가 다 찬 경우 WAITING 상태인 파티 초대들을 삭제
			if (party.isFull()) {
				partyInvitationRepository.deleteAllByPartyAndInvitationStatus(partyId, WAITING);
			}
		} else {
			throw new CustomException(NOT_RECRUITING_PARTY);
		}
	}

	// 호스트가 파티 초대 취소
	@Transactional
	public void cancelInvitedPartyInvitation(Long hostId, Long partyId, PartyInvitation partyInvitation) {
		// 초대 스테이터스가 대기가 아닌 경우
		validateWaitingInvitationType(partyInvitation.getInvitationStatus());
		// 해당 파티의 초대가 아닌 경우
		validInvitationOfParty(partyInvitation, partyId);

		Party party = entityFetcher.getPartyOrThrow(partyId);

		// 호스트가 아닌 경우
		if (!party.isHostOfParty(hostId)) {
			throw new CustomException(NOT_PARTY_HOST);
		}

		partyInvitationRepository.deleteById(partyInvitation.getId());
	}

	// 호스트가 파티 가입 요청 거절
	@Transactional
	public void rejectRequestedPartyInvitation(Long hostId, Long partyId, PartyInvitation partyInvitation) {
		// 초대 스테이터스가 대기가 아닌 경우
		validateWaitingInvitationType(partyInvitation.getInvitationStatus());
		// 해당 파티의 초대가 아닌 경우
		validInvitationOfParty(partyInvitation, partyId);

		Party party = entityFetcher.getPartyOrThrow(partyId);

		// 호스트가 아닌 경우
		if (!party.isHostOfParty(hostId)) {
			throw new CustomException(NOT_PARTY_HOST);
		}

		partyInvitation.updateInvitationStatus(InvitationStatus.REJECTED);
	}

	// 호스트가 랜덤 파티 초대 거절
	@Transactional
	@MatchEventPublish(matchJobType = MatchJobType.USER_MATCH)
	public List<Long> rejectRandomPartyInvitation(
		Long hostId, Long partyId, PartyInvitation partyInvitation) {
		// 초대 스테이터스가 대기가 아닌 경우
		validateWaitingInvitationType(partyInvitation.getInvitationStatus());
		// 해당 파티의 초대가 아닌 경우
		validInvitationOfParty(partyInvitation, partyId);

		Party party = entityFetcher.getPartyOrThrow(partyId);

		// 호스트가 아닌 경우
		if (!party.isHostOfParty(hostId)) {
			throw new CustomException(NOT_PARTY_HOST);
		}

		partyInvitation.updateInvitationStatus(InvitationStatus.REJECTED);

		return List.of(partyInvitation.getUserMatchInfo().getId());    // 매칭 대상이 될 유저 매칭 조건 ID return
	}

	private void validateWaitingInvitationType(InvitationStatus status) {
		if (!status.equals(WAITING)) {
			throw new CustomException(INVALID_PARTY_INVITATION);
		}
	}

	private void validateRecruitingParty(Party party) {
		if (party.isStatus(PartyStatus.ACTIVE)) {
			throw new CustomException(NOT_RECRUITING_PARTY);
		}
	}

	private void validInvitationOfParty(PartyInvitation partyInvitation, Long partyId) {
		if (!partyInvitation.getParty().getId().equals(partyId)) {
			throw new CustomException(UNAUTHORIZED_PARTY_INVITATION);
		}
	}
}
