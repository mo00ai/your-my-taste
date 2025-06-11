package com.example.taste.domain.party.service;

import static com.example.taste.domain.party.exception.PartyErrorCode.ALREADY_EXISTS_PARTY_INVITATION;
import static com.example.taste.domain.party.exception.PartyErrorCode.INVALID_PARTY_INVITATION;
import static com.example.taste.domain.party.exception.PartyErrorCode.NOT_RECRUITING_PARTY;
import static com.example.taste.domain.party.exception.PartyErrorCode.PARTY_INVITATION_NOT_FOUND;
import static com.example.taste.domain.party.exception.PartyErrorCode.UNAUTHORIZED_PARTY;
import static com.example.taste.domain.party.exception.PartyErrorCode.UNAUTHORIZED_PARTY_INVITATION;
import static com.example.taste.domain.user.exception.UserErrorCode.DEACTIVATED_USER;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.taste.common.exception.CustomException;
import com.example.taste.common.util.EntityFetcher;
import com.example.taste.domain.match.entity.PartyMatchCond;
import com.example.taste.domain.match.entity.UserMatchCond;
import com.example.taste.domain.match.repository.PartyMatchCondRepository;
import com.example.taste.domain.party.dto.request.InvitationActionRequestDto;
import com.example.taste.domain.party.dto.request.PartyInvitationRequestDto;
import com.example.taste.domain.party.dto.response.PartyInvitationResponseDto;
import com.example.taste.domain.party.dto.response.UserInvitationResponseDto;
import com.example.taste.domain.party.entity.Party;
import com.example.taste.domain.party.entity.PartyInvitation;
import com.example.taste.domain.party.enums.InvitationStatus;
import com.example.taste.domain.party.enums.InvitationType;
import com.example.taste.domain.party.enums.MatchStatus;
import com.example.taste.domain.party.enums.PartyStatus;
import com.example.taste.domain.party.repository.PartyInvitationRepository;
import com.example.taste.domain.user.entity.User;

@Service
@RequiredArgsConstructor
public class PartyInvitationService {
	private final EntityFetcher entityFetcher;
	private final PartyInvitationRepository partyInvitationRepository;
	private final PartyMatchCondRepository partyMatchCondRepository;

	@Transactional
	public void leaveParty(Long userId, Long partyId) {
		// 파티 초대 가져와서 상태 변경
		PartyInvitation partyInvitation = partyInvitationRepository.findByUserAndParty(userId, partyId)
			.orElseThrow(() -> new CustomException(PARTY_INVITATION_NOT_FOUND));
		partyInvitation.leave();

		// 파티에서 현재 멤버 수 차감
		Party party = entityFetcher.getPartyOrThrow(partyId);
		party.leaveMember();
	}

	@Transactional
	public void removePartyMember(Long hostId, Long userId, Long partyId) {
		Party party = entityFetcher.getPartyOrThrow(partyId);
		// 호스트가 아닌 경우
		if (!party.isHostOfParty(hostId)) {
			throw new CustomException(UNAUTHORIZED_PARTY);
		}

		// 파티 초대 가져와서 상태 변경
		PartyInvitation partyInvitation = partyInvitationRepository.findByUserAndParty(userId, partyId)
			.orElseThrow(() -> new CustomException(PARTY_INVITATION_NOT_FOUND));
		partyInvitation.leave();

		// 파티에서 현재 멤버 수 차감
		party.leaveMember();
	}

	public void inviteUserToParty(
		Long hostId, Long partyId, PartyInvitationRequestDto requestDto) {
		Party party = entityFetcher.getPartyOrThrow(partyId);

		// 호스트가 아닌 경우
		if (!party.isHostOfParty(hostId)) {
			throw new CustomException(UNAUTHORIZED_PARTY);
		}

		// 파티 모집 중이 아닌 경우
		validateRecruitingParty(party);

		// 유저 가져오기, 탈퇴 유저인 경우 예외 발생
		User user = getActiveUserOrElseThrow(requestDto.getUserId());

		// 파티 초대 정보가 있다면 (초대 중, 이미 존재, 거절)
		PartyInvitation partyInvitation =
			partyInvitationRepository.findByUserAndParty(requestDto.getUserId(), partyId)
				.orElse(null);
		if (partyInvitation != null
			&& !partyInvitation.getInvitationStatus().equals(InvitationStatus.KICKED)) {
			throw new CustomException(ALREADY_EXISTS_PARTY_INVITATION);
		}

		partyInvitationRepository.save(new PartyInvitation(
			party, user, InvitationType.INVITATION, InvitationStatus.WAITING));
	}

	private User getActiveUserOrElseThrow(Long userId) {
		User user = entityFetcher.getUserOrThrow(userId);
		if (user.getDeletedAt() != null) {
			throw new CustomException(DEACTIVATED_USER);
		}
		return user;
	}

	public void joinIntoParty(Long userId, Long partyId) {
		Party party = entityFetcher.getPartyOrThrow(partyId);

		// 호스트인 경우
		if (party.isHostOfParty(userId)) {
			throw new CustomException(ALREADY_EXISTS_PARTY_INVITATION);
		}

		// 파티 모집 중이 아닌 경우
		validateRecruitingParty(party);

		// 탈퇴한 유저인 경우 예외 발생
		User user = getActiveUserOrElseThrow(userId);

		partyInvitationRepository.save(new PartyInvitation(
			party, user, InvitationType.REQUEST, InvitationStatus.WAITING));
	}

	public List<UserInvitationResponseDto> getMyInvitations(Long userId) {
		List<PartyInvitation> partyInvitationList =
			partyInvitationRepository.findMyActivePartyInvitationList(
				userId, InvitationStatus.WAITING, PartyStatus.RECRUITING);
		return partyInvitationList.stream()
			.map(UserInvitationResponseDto::new).toList();
	}

	public List<PartyInvitationResponseDto> getPartyInvitations(Long hostId, Long partyId) {
		Party party = entityFetcher.getPartyOrThrow(partyId);
		// 호스트가 아닌 경우
		if (!party.isHostOfParty(hostId)) {
			throw new CustomException(UNAUTHORIZED_PARTY);
		}
		List<PartyInvitation> partyInvitationList =
			partyInvitationRepository.findByPartyIdAndInvitationStatus(partyId, InvitationStatus.WAITING);
		return partyInvitationList.stream()
			.map(PartyInvitationResponseDto::new).toList();
	}

	// 호스트가 수동(초대/가입) 파티 초대 수락	// TODO: 일부만 트랜잭션 걸도록 수정 - @윤예진
	@Transactional
	public void confirmManualPartyInvitation(Long hostId, Long partyId, Long partyInvitationId) {
		// 초대 스테이터스가 대기가 아닌 경우
		PartyInvitation partyInvitation = entityFetcher.getPartyInvitationOrThrow(partyInvitationId);
		validateWaitingInvitationType(partyInvitation.getInvitationStatus());

		Party party = entityFetcher.getPartyOrThrow(partyId);
		// 파티 모집 중이 아닌 경우
		validateRecruitingParty(party);

		// 호스트가 아닌 경우
		if (!party.isHostOfParty(hostId)) {
			throw new CustomException(UNAUTHORIZED_PARTY);
		}

		partyInvitation.setInvitationStatus(InvitationStatus.CONFIRMED);
		if (!party.isFull()) {
			partyInvitation.getParty().joinMember();
			if (party.isFull()) {
				party.setPartyStatus(PartyStatus.FULL);
			}
		} else {
			party.setPartyStatus(PartyStatus.FULL);
			throw new CustomException(NOT_RECRUITING_PARTY);
		}
	}

	// 호스트가 수동(초대/가입) 파티 초대 거절/취소
	public void rejectManualPartyInvitation(Long hostId, Long partyId, Long partyInvitationId,
		InvitationActionRequestDto requestDto) {
		// 초대 스테이터스가 대기가 아닌 경우
		PartyInvitation partyInvitation = entityFetcher.getPartyInvitationOrThrow(partyInvitationId);
		validateWaitingInvitationType(partyInvitation.getInvitationStatus());

		Party party = entityFetcher.getPartyOrThrow(partyId);
		// 파티 모집 중이 아닌 경우
		validateRecruitingParty(party);

		// 호스트가 아닌 경우
		if (!party.isHostOfParty(hostId)) {
			throw new CustomException(UNAUTHORIZED_PARTY);
		}

		partyInvitation.setInvitationStatus(
			InvitationStatus.valueOf(requestDto.getInvitationStatus()));
	}

	// 유저가 수동(초대/가입) 파티 초대 수락
	@Transactional
	public void confirmUserManualPartyInvitation(
		Long userId, Long partyInvitationId) {
		// 초대 스테이터스가 대기가 아닌 경우
		PartyInvitation partyInvitation = entityFetcher.getPartyInvitationOrThrow(partyInvitationId);
		validateWaitingInvitationType(partyInvitation.getInvitationStatus());

		// 내 초대가 아닌 경우
		validOwnerOfPartyInvitation(partyInvitation, userId);

		// 파티 모집 중이 아닌 경우
		Party party = partyInvitation.getParty();
		validateRecruitingParty(party);

		// TODO: 파티가 다 찬 경우 나머지 초대 상태를 비활성화
		if (!party.isFull()) {
			partyInvitation.setInvitationStatus(InvitationStatus.CONFIRMED);
			partyInvitation.getParty().joinMember();
			if (party.isFull()) {
				party.setPartyStatus(PartyStatus.FULL);
			}
		} else {
			party.setPartyStatus(PartyStatus.FULL);
			throw new CustomException(NOT_RECRUITING_PARTY);
		}
	}

	// 유저가 수동(초대/가입) 파티 초대 거절/취소
	public void rejectUserManualPartyInvitation(
		Long userId, Long partyInvitationId, InvitationActionRequestDto requestDto) {
		// 초대 스테이터스가 대기가 아닌 경우
		PartyInvitation partyInvitation = entityFetcher.getPartyInvitationOrThrow(partyInvitationId);
		validateWaitingInvitationType(partyInvitation.getInvitationStatus());

		// 내 초대가 아닌 경우
		validOwnerOfPartyInvitation(partyInvitation, userId);

		// 파티 모집 중이 아닌 경우
		Party party = partyInvitation.getParty();
		validateRecruitingParty(party);

		partyInvitation.setInvitationStatus(
			InvitationStatus.valueOf(requestDto.getInvitationStatus()));
	}

	// 호스트가 랜덤 파티 초대 수락
	@Transactional
	public void confirmRandomPartyInvitation(
		Long hostId, Long partyId, Long partyInvitationId) {
		// 초대 스테이터스가 대기가 아닌 경우
		PartyInvitation partyInvitation = entityFetcher.getPartyInvitationOrThrow(partyInvitationId);
		validateWaitingInvitationType(partyInvitation.getInvitationStatus());

		// 파티 모집 중이 아닌 경우
		Party party = entityFetcher.getPartyOrThrow(partyId);
		validateRecruitingParty(party);

		// 호스트가 아닌 경우
		if (!party.isHostOfParty(hostId)) {
			throw new CustomException(UNAUTHORIZED_PARTY);
		}

		// 매칭 상태가 WAITING_HOST 가 아닌 경우
		UserMatchCond userMatchCond = partyInvitation.getUserMatchCond();
		if (!userMatchCond.isStatus(MatchStatus.WAITING_HOST)) {
			throw new CustomException(INVALID_PARTY_INVITATION);
		}

		if (!party.isFull()) {
			userMatchCond.setMatchStatus(MatchStatus.WAITING_USER);
			partyInvitation.getParty().joinMember();

			if (party.isFull()) {
				party.setPartyStatus(PartyStatus.FULL);
			}
		} else {
			party.setPartyStatus(PartyStatus.FULL);
			throw new CustomException(NOT_RECRUITING_PARTY);
		}
	}

	// 호스트가 랜덤 파티 초대 거절
	public void rejectRandomPartyInvitation(
		Long hostId, Long partyId,
		Long partyInvitationId, InvitationActionRequestDto requestDto) {
		// 초대 스테이터스가 대기가 아닌 경우
		PartyInvitation partyInvitation = entityFetcher.getPartyInvitationOrThrow(partyInvitationId);
		validateWaitingInvitationType(partyInvitation.getInvitationStatus());

		// 파티 모집 중이 아닌 경우
		Party party = entityFetcher.getPartyOrThrow(partyId);
		validateRecruitingParty(party);

		// 호스트가 아닌 경우
		if (!party.isHostOfParty(hostId)) {
			throw new CustomException(UNAUTHORIZED_PARTY);
		}

		partyInvitation.setInvitationStatus(
			InvitationStatus.valueOf(requestDto.getInvitationStatus()));
	}

	// 유저가 랜덤 파티 초대 수락
	@Transactional
	public void confirmUserRandomPartyInvitation(
		Long userId, Long partyInvitationId) {
		// 초대 스테이터스가 대기가 아닌 경우
		PartyInvitation partyInvitation = entityFetcher.getPartyInvitationOrThrow(partyInvitationId);
		validateWaitingInvitationType(partyInvitation.getInvitationStatus());

		// 내 초대가 아닌 경우
		validOwnerOfPartyInvitation(partyInvitation, userId);

		// 파티 모집 중이 아닌 경우
		Party party = partyInvitation.getParty();
		validateRecruitingParty(party);

		// 매칭 상태가 WAITING_USER 가 아닌 경우
		UserMatchCond userMatchCond = partyInvitation.getUserMatchCond();
		if (!userMatchCond.isStatus(MatchStatus.WAITING_HOST)) {
			throw new CustomException(INVALID_PARTY_INVITATION);
		}

		if (!party.isFull()) {
			partyInvitation.setInvitationStatus(InvitationStatus.CONFIRMED);
			userMatchCond.setMatchStatus(MatchStatus.IDLE);

			if (party.isFull()) {
				party.setPartyStatus(PartyStatus.FULL);
				PartyMatchCond partyMatchCond =
					partyMatchCondRepository.findPartyMatchCondByParty(party);
				partyMatchCond.setMatchStatus(MatchStatus.IDLE);
			}
		} else {
			party.setPartyStatus(PartyStatus.FULL);
			PartyMatchCond partyMatchCond =
				partyMatchCondRepository.findPartyMatchCondByParty(party);
			partyMatchCond.setMatchStatus(MatchStatus.IDLE);
			throw new CustomException(NOT_RECRUITING_PARTY);
		}
	}

	// 유저가 랜덤 파티 초대 거절/취소
	public void rejectUserRandomPartyInvitation(
		Long userId, Long partyInvitationId, InvitationActionRequestDto requestDto) {
		// 초대 스테이터스가 대기가 아닌 경우
		PartyInvitation partyInvitation = entityFetcher.getPartyInvitationOrThrow(partyInvitationId);
		validateWaitingInvitationType(partyInvitation.getInvitationStatus());

		// 내 초대가 아닌 경우
		validOwnerOfPartyInvitation(partyInvitation, userId);

		// 파티 모집 중이 아닌 경우
		Party party = partyInvitation.getParty();
		validateRecruitingParty(party);

		partyInvitation.setInvitationStatus(
			InvitationStatus.valueOf(requestDto.getInvitationStatus()));
	}

	private void validateManualType(String type) {
		if ((!type.equals(InvitationType.REQUEST.toString()) ||
			!type.equals(InvitationType.INVITATION.toString()))) {
			throw new CustomException(INVALID_PARTY_INVITATION);
		}
	}

	private void validateWaitingInvitationType(InvitationStatus status) {
		if (!status.equals(InvitationStatus.WAITING)) {
			throw new CustomException(INVALID_PARTY_INVITATION);
		}
	}

	private void validateRecruitingParty(Party party) {
		if (!party.getPartyStatus().equals(PartyStatus.RECRUITING)) {
			throw new CustomException(NOT_RECRUITING_PARTY);
		}
	}

	private void validOwnerOfPartyInvitation(PartyInvitation partyInvitation, Long userId) {
		if (!partyInvitation.getUser().getId().equals(userId)) {
			throw new CustomException(UNAUTHORIZED_PARTY_INVITATION);
		}
	}
}
