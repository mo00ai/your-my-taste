package com.example.taste.domain.party.service;

import static com.example.taste.domain.party.exception.PartyErrorCode.MAX_CAPACITY_LESS_THAN_CURRENT;
import static com.example.taste.domain.party.exception.PartyErrorCode.UNAUTHORIZED_PARTY;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.taste.common.exception.CustomException;
import com.example.taste.common.util.EntityFetcher;
import com.example.taste.domain.party.dto.request.PartyCreateRequestDto;
import com.example.taste.domain.party.dto.request.PartyDetailUpdateRequestDto;
import com.example.taste.domain.party.dto.response.PartyDetailResponseDto;
import com.example.taste.domain.party.dto.response.PartyResponseDto;
import com.example.taste.domain.party.entity.Party;
import com.example.taste.domain.party.entity.PartyInvitation;
import com.example.taste.domain.party.enums.InvitationStatus;
import com.example.taste.domain.party.enums.InvitationType;
import com.example.taste.domain.party.enums.PartyFilter;
import com.example.taste.domain.party.repository.PartyInvitationRepository;
import com.example.taste.domain.party.repository.PartyRepository;
import com.example.taste.domain.store.repository.StoreRepository;
import com.example.taste.domain.user.dto.response.UserSimpleResponseDto;
import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class PartyService {
	private final EntityFetcher entityFetcher;
	private final UserRepository userRepository;
	private final StoreRepository storeRepository;
	private final PartyRepository partyRepository;
	private final PartyInvitationRepository partyInvitationRepository;

	public void createParty(Long hostId, PartyCreateRequestDto requestDto) {
		User hostUser = entityFetcher.getUserOrThrow(hostId);
		// 생성 시점에 맛집이 DB에 없어도 맛집 검색 API 로 추가했다고 가정
		Party party = partyRepository.save(new Party(requestDto, hostUser));
		partyInvitationRepository.save(new PartyInvitation(
			party, hostUser, InvitationType.INVITATION, InvitationStatus.CONFIRMED));
	}

	// TODO: 정렬 기준 추가
	public List<PartyResponseDto> getParties(Long userId, String filter) {
		PartyFilter partyFilter = PartyFilter.of(filter);

		switch (partyFilter) {
			case ALL:
				// 유저가 열고 있는 파티 제외하고 모든 파티 보여줌
				return partyRepository.findAllByRecruitingAndUserNotIn(userId).stream()
					.map(PartyResponseDto::new)
					.toList();
			case MY:
				// 유저가 참가, 호스트인 파티 모두 보여줌 // TODO: 유저가 호스트면 status 상관없이 보여줄지?
				return partyRepository.findAllByRecruitingUserIn(userId).stream()
					.map(PartyResponseDto::new)
					.toList();
			default:
				return partyRepository.findAllByRecruitingAndUserNotIn(userId).stream()
					.map(PartyResponseDto::new)
					.toList();
		}
	}

	public PartyDetailResponseDto getPartyDetail(Long userId, Long partyId) {
		Party party = entityFetcher.getPartyOrThrow(partyId);
		User host = party.getHostUser();

		List<User> members = partyInvitationRepository.findUsersInParty(partyId);
		members.remove(host);        // 파티인원 목록에서 호스트는 제거

		UserSimpleResponseDto hostDto = new UserSimpleResponseDto(host);
		List<UserSimpleResponseDto> membersDtoList =
			members.stream().map(UserSimpleResponseDto::new).toList();

		return new PartyDetailResponseDto(
			party, hostDto, membersDtoList);
	}

	@Transactional
	public void updatePartyDetail(
		Long hostId, Long partyId, PartyDetailUpdateRequestDto requestDto) {
		Party party = entityFetcher.getPartyOrThrow(partyId);

		// 호스트가 아니라면
		if (!isHostOfParty(party, hostId)) {
			throw new CustomException(UNAUTHORIZED_PARTY);
		}

		// 최대 인원 변경하는 경우
		if (requestDto.getMaxMembers() != null) {
			if (requestDto.getMaxMembers() < party.getNowMembers()) {
				throw new CustomException(MAX_CAPACITY_LESS_THAN_CURRENT);
			}
		}

		// 장소 바꾸는 경우
		// 생성 시점에 맛집이 DB에 없어도 맛집 검색 API 로 추가했다고 가정
		party.update(requestDto);
	}

	private boolean isHostOfParty(Party party, Long hostId) {
		return party.getHostUser().getId().equals(hostId);
	}
}
