package com.example.taste.domain.match.service;

import static com.example.taste.domain.party.enums.InvitationStatus.CONFIRMED;
import static com.example.taste.domain.party.enums.InvitationStatus.WAITING;
import static com.example.taste.domain.party.exception.PartyErrorCode.ALREADY_EXISTS_PARTY_INVITATION;
import static com.example.taste.domain.party.exception.PartyErrorCode.INVALID_PARTY_INVITATION;
import static com.example.taste.domain.party.exception.PartyErrorCode.NOT_PARTY_HOST;
import static com.example.taste.domain.party.exception.PartyErrorCode.PARTY_INVITATION_NOT_FOUND;
import static com.example.taste.domain.party.exception.PartyErrorCode.PARTY_NOT_FOUND;
import static com.example.taste.domain.party.exception.PartyErrorCode.UNAVAILABLE_TO_REQUEST_PARTY_INVITATION;
import static com.example.taste.domain.user.exception.UserErrorCode.NOT_FOUND_USER;

import java.util.Comparator;
import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.taste.common.exception.CustomException;
import com.example.taste.common.service.RedisService;
import com.example.taste.domain.match.dto.PartyMatchInfoDto;
import com.example.taste.domain.match.repository.PartyMatchInfoRepository;
import com.example.taste.domain.party.dto.request.PartyInvitationRequestDto;
import com.example.taste.domain.party.dto.response.PartyInvitationResponseDto;
import com.example.taste.domain.party.dto.response.UserInvitationResponseDto;
import com.example.taste.domain.party.entity.Party;
import com.example.taste.domain.party.entity.PartyInvitation;
import com.example.taste.domain.party.enums.InvitationType;
import com.example.taste.domain.party.repository.PartyInvitationRepository;
import com.example.taste.domain.party.repository.PartyRepository;
import com.example.taste.domain.party.validator.PartyInvitationValidator;
import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class PartyInvitationService {
	private final RedisService redisService;
	private final UserRepository userRepository;
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
		Party party = partyRepository.findById(partyId)
			.orElseThrow(() -> new CustomException(PARTY_NOT_FOUND));
		party.leaveMember();

		// 호스트가 나가는 경우(호스트 탈퇴 후에도 멤버가 남아있다면) 참가한지 오래된 유저에게 호스트 위임
		if (party.getNowMembers() > 1 && party.isHostOfParty(userId)) {
			// 오름차순 정렬
			List<PartyInvitation> partyInvitationList =
				partyInvitationRepository.findByPartyAndInvitationStatus(party.getId(), CONFIRMED);
			User newHostUser = partyInvitationList.stream()
				.filter(pi -> !pi.getUser().getId().equals(userId)) // 호스트 제외
				.min(Comparator.comparing(PartyInvitation::getCreatedAt))
				.orElseThrow(() -> new CustomException(PARTY_INVITATION_NOT_FOUND)).getUser();

			party.updateHost(newHostUser);
		}

		// 랜덤 매칭 상태인 경우 캐시에 평균 나이 업데이트
		updatePartyMatchInfoCacheAfterLeave(party, partyInvitation.getUser().getAge());
	}

	// 강퇴
	@Transactional
	public void removePartyMember(Long hostId, Long userId, Long partyId) {
		Party party = partyRepository.findById(partyId)
			.orElseThrow(() -> new CustomException(PARTY_NOT_FOUND));
		// 호스트가 아닌 경우
		if (!party.isHostOfParty(hostId)) {
			throw new CustomException(NOT_PARTY_HOST);
		}

		// 파티 초대 가져와서 상태 변경
		PartyInvitation partyInvitation = partyInvitationRepository.findByUserAndParty(userId, partyId)
			.orElseThrow(() -> new CustomException(PARTY_INVITATION_NOT_FOUND));
		partyInvitation.leave();

		// 파티에서 현재 멤버 수 차감
		party.leaveMember();

		// 랜덤 매칭 상태인 경우 캐시에 평균 나이 업데이트
		updatePartyMatchInfoCacheAfterLeave(party, partyInvitation.getUser().getAge());
	}

	public void inviteUserToParty(
		Long hostId, Long partyId, PartyInvitationRequestDto requestDto) {
		Party party = partyRepository.findById(partyId)
			.orElseThrow(() -> new CustomException(PARTY_NOT_FOUND));

		// 호스트가 아닌데 초대 시도 하는 경우
		if (!party.isHostOfParty(hostId)) {
			throw new CustomException(NOT_PARTY_HOST);
		}

		// 호스트가 자기 자신 초대하는 경우
		if (party.isHostOfParty(requestDto.getUserId())) {
			throw new CustomException(ALREADY_EXISTS_PARTY_INVITATION);
		}

		// 파티 모집 중이 아닌 경우
		PartyInvitationValidator.validateAvailableToJoin(party);

		// 유저 가져오기, 탈퇴 유저인 경우 예외 발생
		User user = userRepository.findByIdAndDeletedAtIsNull(requestDto.getUserId())
			.orElseThrow(() -> new CustomException(NOT_FOUND_USER));

		// 파티 초대 정보가 있다면 (초대 중, 확정)
		partyInvitationRepository.findByUserAndParty(requestDto.getUserId(), partyId)
			.ifPresent((pi) -> {
					// 파티 수라가 대기-가입된 유저 재초대하는 경우 예외 발생
					if (pi.isStatus(WAITING) || pi.isStatus(CONFIRMED)) {
						throw new CustomException(ALREADY_EXISTS_PARTY_INVITATION);
					}
					// 그 외 재초대 가능한 경우 삭제 후 새로 생성
					else {
						partyInvitationRepository.deleteById(pi.getId());
					}
				}
			);

		partyInvitationRepository.save(new PartyInvitation(
			party, user, InvitationType.INVITATION, WAITING));
	}

	// 유저가 파티에 가입 신청
	public void joinIntoParty(Long userId, Long partyId) {
		Party party = partyRepository.findById(partyId)
			.orElseThrow(() -> new CustomException(PARTY_NOT_FOUND));

		// 호스트가 자신의 파티에 가입 신청하는 경우
		if (party.isHostOfParty(userId)) {
			throw new CustomException(ALREADY_EXISTS_PARTY_INVITATION);
		}

		// 파티 모집 중이 아닌 경우
		PartyInvitationValidator.validateAvailableToJoin(party);

		// 초대 정보가 존재하는 경우
		partyInvitationRepository.findByUserAndParty(userId, partyId)
			.ifPresent((pi) -> {
				switch (pi.getInvitationStatus()) {
					case EXITED -> {
					} // 가입 신청 가능
					case KICKED, REJECTED -> throw new CustomException(UNAVAILABLE_TO_REQUEST_PARTY_INVITATION);
					case WAITING, CONFIRMED -> throw new CustomException(ALREADY_EXISTS_PARTY_INVITATION);
					default -> throw new CustomException(INVALID_PARTY_INVITATION);
				}
			});

		User user = userRepository.findByIdAndDeletedAtIsNull(userId)
			.orElseThrow(() -> new CustomException(NOT_FOUND_USER));
		partyInvitationRepository.save(
			new PartyInvitation(party, user, InvitationType.REQUEST, WAITING));
	}

	public List<UserInvitationResponseDto> getMyInvitations(Long userId) {
		List<PartyInvitation> partyInvitationList =
			partyInvitationRepository.findAvailablePartyInvitationList(
				userId, WAITING);
		return partyInvitationList.stream()
			.map(UserInvitationResponseDto::new).toList();
	}

	public List<PartyInvitationResponseDto> getPartyInvitations(Long hostId, Long partyId) {
		Party party = partyRepository.findById(partyId)
			.orElseThrow(() -> new CustomException(PARTY_NOT_FOUND));
		// 호스트가 아닌 경우
		if (!party.isHostOfParty(hostId)) {
			throw new CustomException(NOT_PARTY_HOST);
		}
		List<PartyInvitation> partyInvitationList =
			partyInvitationRepository.findByPartyAndInvitationStatus(party.getId(), WAITING);
		return partyInvitationList.stream()
			.map(PartyInvitationResponseDto::new).toList();
	}

	private void updatePartyMatchInfoCacheAfterLeave(Party party, int userAge) {
		partyMatchInfoRepository.findIdByPartyId(party.getId())
			.ifPresent((partyMatchInfoId) -> {
				String key = "partyMatchInfo" + partyMatchInfoId;
				PartyMatchInfoDto cachedDto = (PartyMatchInfoDto)redisService.getKeyValue(key);
				if (cachedDto != null) {
					PartyMatchInfoDto newCacheDto = new PartyMatchInfoDto(
						cachedDto, party.calculateAvgAgeAfterLeave(cachedDto.getAvgAge(), userAge));
					redisService.setKeyValue(key, newCacheDto);
				}
			});
	}
}
