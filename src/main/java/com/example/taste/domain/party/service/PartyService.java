package com.example.taste.domain.party.service;

import static com.example.taste.common.exception.ErrorCode.INVALID_INPUT_VALUE;
import static com.example.taste.domain.match.exception.MatchErrorCode.ACTIVE_MATCH_EXISTS;
import static com.example.taste.domain.party.exception.PartyErrorCode.MAX_CAPACITY_LESS_THAN_CURRENT;
import static com.example.taste.domain.party.exception.PartyErrorCode.NOT_PARTY_HOST;
import static com.example.taste.domain.party.exception.PartyErrorCode.PARTY_NOT_FOUND;
import static com.example.taste.domain.store.exception.StoreErrorCode.STORE_NOT_FOUND;
import static com.example.taste.domain.user.exception.UserErrorCode.NOT_FOUND_USER;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.taste.common.exception.CustomException;
import com.example.taste.domain.match.dto.request.PartyMatchInfoCreateRequestDto;
import com.example.taste.domain.match.service.MatchService;
import com.example.taste.domain.party.dto.request.PartyCreateRequestDto;
import com.example.taste.domain.party.dto.request.PartyUpdateRequestDto;
import com.example.taste.domain.party.dto.response.PartyDetailResponseDto;
import com.example.taste.domain.party.dto.response.PartyResponseDto;
import com.example.taste.domain.party.entity.Party;
import com.example.taste.domain.party.entity.PartyInvitation;
import com.example.taste.domain.party.enums.InvitationStatus;
import com.example.taste.domain.party.enums.InvitationType;
import com.example.taste.domain.party.enums.PartyFilter;
import com.example.taste.domain.party.repository.PartyInvitationRepository;
import com.example.taste.domain.party.repository.PartyRepository;
import com.example.taste.domain.store.entity.Store;
import com.example.taste.domain.store.repository.StoreRepository;
import com.example.taste.domain.user.dto.response.UserSimpleResponseDto;
import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class PartyService {        // TODO: 파티 만료 시 / 파티 다 찼을 시, 파티 초대 정보, 파티 채팅 등 연관 정보 삭제하는 기능 추후 추가 - @윤예진
	private final MatchService matchService;
	private final UserRepository userRepository;
	private final StoreRepository storeRepository;
	private final PartyRepository partyRepository;
	private final PartyInvitationRepository partyInvitationRepository;

	@Transactional
	public void createParty(Long hostId, PartyCreateRequestDto requestDto) {
		// 생성 시점에 맛집이 DB에 없어도 맛집 검색 API 로 추가했다고 가정
		Store store = null;
		if (requestDto.getStoreId() != null) {
			store = storeRepository.findById(requestDto.getStoreId())
				.orElseThrow(() -> new CustomException(STORE_NOT_FOUND));
		}

		User hostUser = userRepository.findById(hostId)
			.orElseThrow(() -> new CustomException(NOT_FOUND_USER));
		Party party = partyRepository.save(new Party(requestDto, hostUser, store));
		partyInvitationRepository.save(new PartyInvitation(
			party, hostUser, InvitationType.INVITATION, InvitationStatus.CONFIRMED));

		// 파티 생성하며 랜덤 매칭도 같이 신청하는 경우
		if (requestDto.getEnableRandomMatching()) {
			matchService.registerPartyMatch(hostUser.getId(),
				new PartyMatchInfoCreateRequestDto(party.getId(), requestDto.getPartyMatchInfo()));
		}
	}

	// TODO: 정렬 기준 추가 - @윤예진
	public List<PartyResponseDto> getParties(Long userId, String filter) {
		PartyFilter partyFilter = PartyFilter.of(filter);
		switch (partyFilter) {
			case ALL -> {
				// 유저가 참가한 파티 제외하고 모든 파티 보여줌
				return partyRepository.findAllByRecruitingAndUserNotIn(userId).stream()
					.map(PartyResponseDto::new)
					.toList();
			}
			case MY -> {
				// 유저가 참가, 호스트인 파티 모두 보여줌
				return partyRepository.findAllByUserIn(userId).stream()
					.map(PartyResponseDto::new)
					.toList();
			}
			default -> throw new CustomException(INVALID_INPUT_VALUE);
		}
	}

	public PartyDetailResponseDto getPartyDetail(Long partyId) {
		Party party = partyRepository.findById(partyId)
			.orElseThrow(() -> new CustomException(PARTY_NOT_FOUND));
		User host = party.getHostUser();

		List<User> members = partyInvitationRepository.findUsersInParty(partyId);
		members.remove(host);        // 파티인원 목록에서 호스트는 제거

		UserSimpleResponseDto hostDto = new UserSimpleResponseDto(host);
		List<UserSimpleResponseDto> membersDtoList =
			members.stream().map(UserSimpleResponseDto::new).toList();

		return new PartyDetailResponseDto(party, hostDto, membersDtoList);
	}

	@Transactional
	public void updatePartyDetail(
		Long hostId, Long partyId, PartyUpdateRequestDto requestDto) {
		Party party = partyRepository.findById(partyId)
			.orElseThrow(() -> new CustomException(PARTY_NOT_FOUND));

		// 호스트가 아니라면
		if (!party.isHostOfParty(hostId)) {
			throw new CustomException(NOT_PARTY_HOST);
		}

		// 랜덤 매칭 중인 경우
		if (party.isEnableRandomMatching()) {
			throw new CustomException(ACTIVE_MATCH_EXISTS);
		}

		// 최대 인원 변경하는 경우
		if (requestDto.getMaxMembers() != null) {
			if (requestDto.getMaxMembers() < party.getNowMembers()) {
				throw new CustomException(MAX_CAPACITY_LESS_THAN_CURRENT);
			}
		}

		// 장소 바꾸는 경우
		if (requestDto.getStoreId() != null) {
			Store newStore = storeRepository.findById(requestDto.getStoreId())
				.orElseThrow(() -> new CustomException(STORE_NOT_FOUND));
			party.update(requestDto, newStore);
			return;
		}

		party.update(requestDto);
	}

	@Transactional
	public void removeParty(Long hostId, Long partyId) {
		Party party = partyRepository.findById(partyId)
			.orElseThrow(() -> new CustomException(PARTY_NOT_FOUND));

		// 호스트가 아니라면
		if (!party.isHostOfParty(hostId)) {
			throw new CustomException(NOT_PARTY_HOST);
		}

		partyRepository.delete(party);
	}
}
