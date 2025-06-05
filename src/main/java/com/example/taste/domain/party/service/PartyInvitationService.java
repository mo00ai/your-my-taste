package com.example.taste.domain.party.service;

import static com.example.taste.domain.party.exception.PartyErrorCode.ALREADY_EXISTS_PARTY_INVITATION;
import static com.example.taste.domain.party.exception.PartyErrorCode.NOT_RECRUITING_PARTY;
import static com.example.taste.domain.party.exception.PartyErrorCode.PARTY_INVITATION_NOT_FOUND;
import static com.example.taste.domain.party.exception.PartyErrorCode.PARTY_NOT_FOUND;
import static com.example.taste.domain.party.exception.PartyErrorCode.UNAUTHORIZED_PARTY;
import static com.example.taste.domain.user.exception.UserErrorCode.DEACTIVATED_USER;
import static com.example.taste.domain.user.exception.UserErrorCode.USER_NOT_FOUND;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.taste.common.exception.CustomException;
import com.example.taste.domain.party.dto.request.PartyInvitationRequestDto;
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
		Party party = partyRepository.findById(partyId)
			.orElseThrow(() -> new CustomException(PARTY_NOT_FOUND));
		party.leaveMember();
	}

	@Transactional
	public void removePartyMember(Long hostId, Long userId, Long partyId) {
		Party party = partyRepository.findById(partyId)
			.orElseThrow(() -> new CustomException(PARTY_NOT_FOUND));
		// 호스트가 아니라면
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
		Party party = partyRepository.findById(partyId)
			.orElseThrow(() -> new CustomException(PARTY_NOT_FOUND));

		// 호스트가 아닌 경우
		if (!isHostOfParty(party, hostId)) {
			throw new CustomException(UNAUTHORIZED_PARTY);
		}

		// 파티가 모집 중이 아니라면
		if (!party.getPartyStatus().equals(PartyStatus.RECRUITING)) {
			throw new CustomException(NOT_RECRUITING_PARTY);
		}

		// 탈퇴한 유저인 경우
		User user = userRepository.findById(requestDto.getUserId())    // TODO: 레포지토리 메소드로 리팩토링
			.orElseThrow(() -> new CustomException(USER_NOT_FOUND));
		if (user.getDeletedAt() != null) {
			throw new CustomException(DEACTIVATED_USER);
		}

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

	public void joinIntoParty(Long userId, Long partyId) {
		Party party = partyRepository.findById(partyId)
			.orElseThrow(() -> new CustomException(PARTY_NOT_FOUND));

		// 호스트인 경우
		if (isHostOfParty(party, userId)) {
			throw new CustomException(ALREADY_EXISTS_PARTY_INVITATION);
		}

		// 파티가 모집 중이 아니라면
		if (!party.getPartyStatus().equals(PartyStatus.RECRUITING)) {
			throw new CustomException(NOT_RECRUITING_PARTY);
		}

		// 탈퇴한 유저인 경우
		User user = userRepository.findById(userId)    // TODO: 레포지토리 메소드로 리팩토링
			.orElseThrow(() -> new CustomException(USER_NOT_FOUND));
		if (user.getDeletedAt() != null) {
			throw new CustomException(DEACTIVATED_USER);
		}

		partyInvitationRepository.save(new PartyInvitation(
			party, user, InvitationType.REQUEST, InvitationStatus.WAITING));
	}

	private boolean isHostOfParty(Party party, Long hostId) {
		return party.getHostUser().getId().equals(hostId);
	}

}
