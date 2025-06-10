package com.example.taste.domain.party.service;

import static com.example.taste.domain.party.exception.PartyErrorCode.ALREADY_EXISTS_PARTY_INVITATION;
import static com.example.taste.domain.party.exception.PartyErrorCode.INVALID_PARTY_INVITATION;
import static com.example.taste.domain.party.exception.PartyErrorCode.NOT_RECRUITING_PARTY;
import static com.example.taste.domain.party.exception.PartyErrorCode.PARTY_INVITATION_NOT_FOUND;
import static com.example.taste.domain.party.exception.PartyErrorCode.UNAUTHORIZED_PARTY;
import static com.example.taste.domain.user.exception.UserErrorCode.DEACTIVATED_USER;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.taste.common.exception.CustomException;
import com.example.taste.common.util.EntityFetcher;
import com.example.taste.domain.party.dto.request.InvitationActionRequestDto;
import com.example.taste.domain.party.dto.request.PartyInvitationRequestDto;
import com.example.taste.domain.party.dto.response.PartyInvitationResponseDto;
import com.example.taste.domain.party.dto.response.UserInvitationResponseDto;
import com.example.taste.domain.party.entity.Party;
import com.example.taste.domain.party.entity.PartyInvitation;
import com.example.taste.domain.party.enums.InvitationStatus;
import com.example.taste.domain.party.enums.InvitationType;
import com.example.taste.domain.party.enums.PartyStatus;
import com.example.taste.domain.party.repository.PartyInvitationRepository;
import com.example.taste.domain.party.repository.PartyRepository;
import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class PartyInvitationService {
	private final EntityFetcher entityFetcher;
	private final UserRepository userRepository;
	private final PartyRepository partyRepository;
	private final PartyInvitationRepository partyInvitationRepository;

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
		if (!isHostOfParty(party, hostId)) {
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
		if (!isHostOfParty(party, hostId)) {
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
		if (isHostOfParty(party, userId)) {
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
		// TODO: 파티가 모집 중이고(조건 추가), 대기 중인 초대만 가져오기
		List<PartyInvitation> partyInvitationList =
			partyInvitationRepository.findByUserIdAndInvitationStatus(userId, InvitationStatus.WAITING);
		return partyInvitationList.stream()
			.map(UserInvitationResponseDto::new).toList();
	}

	public List<PartyInvitationResponseDto> getPartyInvitations(Long hostId, Long partyId) {
		Party party = entityFetcher.getPartyOrThrow(partyId);
		// 호스트가 아닌 경우
		if (!isHostOfParty(party, hostId)) {
			throw new CustomException(UNAUTHORIZED_PARTY);
		}
		List<PartyInvitation> partyInvitationList =
			partyInvitationRepository.findByPartyIdAndInvitationStatus(partyId, InvitationStatus.WAITING);
		return partyInvitationList.stream()
			.map(PartyInvitationResponseDto::new).toList();
	}

	// 호스트가 파티 초대 수락	// TODO: 일부만 트랜잭션 걸도록 수정
	@Transactional
	public void confirmPartyInvitation(Long hostId, Long partyId, Long partyInvitationId,
		InvitationActionRequestDto requestDto) {
		// 초대 및 수락 타입이 아닌 경우
		validateRequestOrInvitationType(requestDto.getInvitationType());
		// 초대 스테이터스가 대기가 아닌 경우
		validateWaitingInvitationType(requestDto.getInvitationStatus());

		Party party = entityFetcher.getPartyOrThrow(partyId);
		// 파티 모집 중이 아닌 경우
		validateRecruitingParty(party);

		// 호스트가 아닌 경우
		if (!isHostOfParty(party, hostId)) {
			throw new CustomException(UNAUTHORIZED_PARTY);
		}

		PartyInvitation partyInvitation = entityFetcher.getPartyInvitationOrThrow(partyInvitationId);

		partyInvitation.setInvitationStatus(InvitationStatus.CONFIRMED);
		if (!party.isFull()) {
			partyInvitation.getParty().joinMember();
		} else {
			throw new CustomException(NOT_RECRUITING_PARTY);
		}
	}

	// 호스트가 파티 초대 거절/취소
	public void rejectPartyInvitation(Long hostId, Long partyId, Long partyInvitationId,
		InvitationActionRequestDto requestDto) {
		// 초대 및 수락 타입이 아닌 경우
		validateRequestOrInvitationType(requestDto.getInvitationType());
		// 초대 스테이터스가 대기가 아닌 경우
		validateWaitingInvitationType(requestDto.getInvitationStatus());

		Party party = entityFetcher.getPartyOrThrow(partyId);
		// 파티 모집 중이 아닌 경우
		validateRecruitingParty(party);

		// 호스트가 아닌 경우
		if (!isHostOfParty(party, hostId)) {
			throw new CustomException(UNAUTHORIZED_PARTY);
		}

		PartyInvitation partyInvitation = entityFetcher.getPartyInvitationOrThrow(partyInvitationId);

		partyInvitation.setInvitationStatus(
			InvitationStatus.valueOf(requestDto.getInvitationStatus()));
	}

	// 유저가 파티 초대 수락
	@Transactional
	public void confirmUserPartyInvitation(
		Long id, Long partyInvitationId, InvitationActionRequestDto requestDto) {
		// 초대 및 수락 타입이 아닌 경우
		validateRequestOrInvitationType(requestDto.getInvitationType());
		// 초대 스테이터스가 대기가 아닌 경우
		validateWaitingInvitationType(requestDto.getInvitationStatus());

		PartyInvitation partyInvitation = entityFetcher.getPartyInvitationOrThrow(partyInvitationId);
		Party party = partyInvitation.getParty();
		// 파티 모집 중이 아닌 경우
		validateRecruitingParty(party);

		partyInvitation.setInvitationStatus(InvitationStatus.CONFIRMED);
		if (!party.isFull()) {
			partyInvitation.getParty().joinMember();
		} else {
			throw new CustomException(NOT_RECRUITING_PARTY);
		}
	}

	// 유저가 파티 초대 거절/취소
	public void rejectUserPartyInvitation(
		Long id, Long partyInvitationId, InvitationActionRequestDto requestDto) {
		// 초대 및 수락 타입이 아닌 경우
		validateRequestOrInvitationType(requestDto.getInvitationType());
		// 초대 스테이터스가 대기가 아닌 경우
		validateWaitingInvitationType(requestDto.getInvitationStatus());

		PartyInvitation partyInvitation = entityFetcher.getPartyInvitationOrThrow(partyInvitationId);
		Party party = partyInvitation.getParty();
		// 파티 모집 중이 아닌 경우
		validateRecruitingParty(party);

		partyInvitation.setInvitationStatus(
			InvitationStatus.valueOf(requestDto.getInvitationStatus()));
	}

	private boolean isHostOfParty(Party party, Long hostId) {
		return party.getHostUser().getId().equals(hostId);
	}

	private void validateRequestOrInvitationType(String type) {
		if ((!type.equals(InvitationType.REQUEST.toString()) ||
			!type.equals(InvitationType.INVITATION.toString()))) {
			throw new CustomException(INVALID_PARTY_INVITATION);
		}
	}

	private void validateWaitingInvitationType(String status) {
		if (!status.equals(InvitationStatus.WAITING.toString())) {
			throw new CustomException(INVALID_PARTY_INVITATION);
		}
	}

	private void validateRecruitingParty(Party party) {
		if (!party.getPartyStatus().equals(PartyStatus.RECRUITING)) {
			throw new CustomException(NOT_RECRUITING_PARTY);
		}
	}
}
