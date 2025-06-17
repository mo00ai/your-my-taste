package com.example.taste.domain.party.service;

import static com.example.taste.domain.party.exception.PartyErrorCode.ALREADY_EXISTS_PARTY_INVITATION;
import static com.example.taste.domain.party.exception.PartyErrorCode.INVALID_PARTY_INVITATION;
import static com.example.taste.domain.party.exception.PartyErrorCode.NOT_RECRUITING_PARTY;
import static com.example.taste.domain.party.exception.PartyErrorCode.PARTY_INVITATION_NOT_FOUND;
import static com.example.taste.domain.party.exception.PartyErrorCode.UNAUTHORIZED_PARTY;
import static com.example.taste.domain.party.exception.PartyErrorCode.UNAUTHORIZED_PARTY_INVITATION;
import static com.example.taste.domain.party.exception.PartyErrorCode.UNAVAILABLE_TO_INVITE_TO_PARTY;
import static com.example.taste.domain.party.exception.PartyErrorCode.UNAVAILABLE_TO_REQUEST_PARTY_INVITATION;

import java.util.Comparator;
import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.taste.common.exception.CustomException;
import com.example.taste.common.util.EntityFetcher;
import com.example.taste.domain.match.annotation.MatchEventPublish;
import com.example.taste.domain.match.entity.PartyMatchInfo;
import com.example.taste.domain.match.entity.UserMatchInfo;
import com.example.taste.domain.match.enums.MatchJobType;
import com.example.taste.domain.match.repository.PartyMatchInfoRepository;
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
import com.example.taste.domain.party.repository.PartyRepository;
import com.example.taste.domain.user.entity.User;

@Service
@RequiredArgsConstructor
public class PartyInvitationService {
	private final EntityFetcher entityFetcher;
	private final PartyRepository partyRepository;
	private final PartyInvitationRepository partyInvitationRepository;
	private final PartyMatchInfoRepository partyMatchInfoRepository;

	@Transactional
	public void leaveParty(Long userId, Long partyId) {
		// 파티 초대 가져와서 상태 변경
		PartyInvitation partyInvitation = partyInvitationRepository.findByUserAndParty(userId, partyId)
			.orElseThrow(() -> new CustomException(PARTY_INVITATION_NOT_FOUND));
		partyInvitation.leave();

		// 파티에서 현재 멤버 수 차감
		Party party = partyRepository.findByIdWithInvitationsAndUsers(partyId)
			.orElseThrow(() -> new CustomException(PARTY_INVITATION_NOT_FOUND));
		party.leaveMember();

		// 호스트가 나가는 경우(호스트 탈퇴 후에도 멤버가 남아있다면) 참가한지 오래된 유저에게 호스트 위임
		if (party.getNowMembers() > 1 && party.isHostOfParty(userId)) {
			// 오름차순 정렬
			User newHostUser = party.getPartyInvitationList()
				.stream()
				.filter(pi -> !pi.getUser().getId().equals(userId)) // 호스트 제외
				.min(Comparator.comparing(PartyInvitation::getCreatedAt))
				.get().getUser();

			party.updateHost(newHostUser);
		}
	}

	// 강퇴
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
		User user = entityFetcher.getUndeletedUserOrThrow(requestDto.getUserId());

		// 파티 초대 정보가 있다면 (초대 중, 확정)
		PartyInvitation partyInvitation =
			partyInvitationRepository.findByUserAndParty(requestDto.getUserId(), partyId)
				.orElse(null);
		if (partyInvitation != null
			&& (partyInvitation.getInvitationStatus().equals(InvitationStatus.WAITING)
			|| partyInvitation.getInvitationStatus().equals(InvitationStatus.CONFIRMED))) {
			throw new CustomException(ALREADY_EXISTS_PARTY_INVITATION);
		}
		// 초대 불가능하다면 (초대 거절)
		if (partyInvitation != null
			&& partyInvitation.getInvitationType().equals(InvitationType.INVITATION)
			&& partyInvitation.getInvitationStatus().equals(InvitationStatus.REJECTED)) {
			throw new CustomException(UNAVAILABLE_TO_INVITE_TO_PARTY);
		}

		// TODO: 초대 정보가 있고, 재초대 가능한 경우 로직 처리 추가
		partyInvitationRepository.save(new PartyInvitation(
			party, user, InvitationType.INVITATION, InvitationStatus.WAITING));
	}

	// 유저가 파티에 가입 신청
	public void joinIntoParty(Long userId, Long partyId) {
		Party party = entityFetcher.getPartyOrThrow(partyId);

		// 호스트인 경우
		if (party.isHostOfParty(userId)) {
			throw new CustomException(ALREADY_EXISTS_PARTY_INVITATION);
		}

		// 파티 모집 중이 아닌 경우
		validateRecruitingParty(party);

		// 탈퇴한 유저인 경우 예외 발생
		User user = entityFetcher.getUndeletedUserOrThrow(userId);

		// 초대 정보가 존재하는 경우
		PartyInvitation partyInvitation = partyInvitationRepository.findByUserAndParty(userId, partyId).orElse(null);
		if (partyInvitation != null) {
			switch (partyInvitation.getInvitationStatus()) {
				case EXITED -> {
				} // 가입 신청 가능
				case KICKED, REJECTED -> throw new CustomException(UNAVAILABLE_TO_REQUEST_PARTY_INVITATION);
				case WAITING, CONFIRMED -> throw new CustomException(ALREADY_EXISTS_PARTY_INVITATION);
				default -> throw new CustomException(INVALID_PARTY_INVITATION);
			}
		}

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
			partyInvitationRepository.findAllByPartyAndInvitationStatus(party, InvitationStatus.WAITING);
		return partyInvitationList.stream()
			.map(PartyInvitationResponseDto::new).toList();
	}

	// 호스트가 수동(초대/가입) 파티 초대 수락
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
		partyInvitation.updateInvitationStatus(InvitationStatus.CONFIRMED);

		if (!party.isFull()) {
			partyInvitation.getParty().joinMember();
			// 파티가 다 찬 경우 WAITING 상태인 파티 초대들을 삭제
			if (party.isFull()) {
				partyInvitationRepository.deleteAllByPartyAndInvitationStatus(partyId, InvitationStatus.WAITING);
			}
		} else {
			throw new CustomException(NOT_RECRUITING_PARTY);
		}
	}

	// 호스트가 수동(초대/가입) 파티 초대 거절/취소
	@Transactional
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

		partyInvitation.updateInvitationStatus(
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

		if (!party.isFull()) {
			partyInvitation.updateInvitationStatus(InvitationStatus.CONFIRMED);
			partyInvitation.getParty().joinMember();            // TODO: 수락하기 전 invitation status 가 waiting 이 아니라면 예외처리

			// 파티가 다 찬 경우 WAITING 상태인 파티 초대들을 삭제
			if (party.isFull()) {
				partyInvitationRepository.deleteAllByPartyAndInvitationStatus(party.getId(), InvitationStatus.WAITING);
			}
		} else {
			throw new CustomException(NOT_RECRUITING_PARTY);
		}
	}

	// 유저가 수동(초대/가입) 파티 초대 거절/취소
	@Transactional
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

		partyInvitation.updateInvitationStatus(
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
		UserMatchInfo userMatchInfo = partyInvitation.getUserMatchInfo();
		if (!userMatchInfo.isStatus(MatchStatus.WAITING_HOST)) {
			throw new CustomException(INVALID_PARTY_INVITATION);
		}

		if (!party.isFull()) {
			userMatchInfo.updateMatchStatus(MatchStatus.WAITING_USER);

			// 파티가 다 찬 경우 WAITING 상태인 파티 초대들을 삭제
			if (party.isFull()) {
				partyInvitationRepository.deleteAllByPartyAndInvitationStatus(partyId, InvitationStatus.WAITING);
			}
		} else {
			// 파티가 다 찬 경우 WAITING 상태인 파티 초대들을 삭제
			partyInvitationRepository.deleteAllByPartyAndInvitationStatus(partyId, InvitationStatus.WAITING);
			throw new CustomException(NOT_RECRUITING_PARTY);
		}
	}

	// 호스트가 랜덤 파티 초대 거절
	@Transactional
	@MatchEventPublish(matchJobType = MatchJobType.USER_MATCH)
	public List<Long> rejectRandomPartyInvitation(
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

		partyInvitation.updateInvitationStatus(
			InvitationStatus.valueOf(requestDto.getInvitationStatus()));

		return List.of(partyInvitation.getUserMatchInfo().getId());    // 매칭 대상이 될 유저 매칭 조건 ID
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
		UserMatchInfo userMatchInfo = partyInvitation.getUserMatchInfo();
		if (!userMatchInfo.isStatus(MatchStatus.WAITING_USER)) {
			throw new CustomException(INVALID_PARTY_INVITATION);
		}

		if (!party.isFull()) {
			partyInvitation.updateInvitationStatus(InvitationStatus.CONFIRMED);
			party.joinMember();
			userMatchInfo.updateMatchStatus(MatchStatus.IDLE);

			if (party.isFull()) {
				PartyMatchInfo partyMatchInfo =
					partyMatchInfoRepository.findPartyMatchInfoByParty(party);
				partyMatchInfo.updateMatchStatus(MatchStatus.IDLE);
				partyInvitationRepository.deleteAllByPartyAndInvitationStatus(party.getId(), InvitationStatus.WAITING);
			}
		} else {
			PartyMatchInfo partyMatchInfo =
				partyMatchInfoRepository.findPartyMatchInfoByParty(party);
			partyMatchInfo.updateMatchStatus(MatchStatus.IDLE);
			partyInvitationRepository.deleteAllByPartyAndInvitationStatus(party.getId(), InvitationStatus.WAITING);
			throw new CustomException(NOT_RECRUITING_PARTY);
		}
	}

	// 유저가 랜덤 파티 초대 거절/취소
	@Transactional
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

		partyInvitation.updateInvitationStatus(
			InvitationStatus.valueOf(requestDto.getInvitationStatus()));
	}

	private void validateManualType(String type) {
		if ((!type.equals(InvitationType.REQUEST.toString()) &&
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
		if (party.isFull()) {
			throw new CustomException(NOT_RECRUITING_PARTY);
		}
	}

	private void validOwnerOfPartyInvitation(PartyInvitation partyInvitation, Long userId) {
		if (!partyInvitation.getUser().getId().equals(userId)) {
			throw new CustomException(UNAUTHORIZED_PARTY_INVITATION);
		}
	}
}
