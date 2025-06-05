package com.example.taste.domain.party.service;

import static com.example.taste.domain.party.exception.PartyErrorCode.MAX_CAPACITY_LESS_THAN_CURRENT;
import static com.example.taste.domain.party.exception.PartyErrorCode.PARTY_NOT_FOUND;
import static com.example.taste.domain.party.exception.PartyErrorCode.UNAUTHORIZED_PARTY;
import static com.example.taste.domain.store.exception.StoreErrorCode.STORE_NOT_FOUND;
import static com.example.taste.domain.user.exception.UserErrorCode.USER_NOT_FOUND;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.taste.common.exception.CustomException;
import com.example.taste.domain.party.dto.reponse.PartyDetailResponseDto;
import com.example.taste.domain.party.dto.reponse.PartyResponseDto;
import com.example.taste.domain.party.dto.request.PartyCreateRequestDto;
import com.example.taste.domain.party.dto.request.PartyDetailUpdateRequestDto;
import com.example.taste.domain.party.entity.Party;
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
public class PartyService {
	private final UserRepository userRepository;
	private final StoreRepository storeRepository;
	private final PartyRepository partyRepository;
	private final PartyInvitationRepository partyInvitationRepository;

	@Transactional
	public void createParty(Long hostId, PartyCreateRequestDto requestDto) {
		User hostUser = userRepository.findById(hostId)
			.orElseThrow(() -> new CustomException(USER_NOT_FOUND));
		if (requestDto.getStoreId() != null) {
			Store store = storeRepository.findById(requestDto.getStoreId())
				.orElseThrow(() -> new CustomException(STORE_NOT_FOUND));
			// TODO: store 에 없을 시 새로 추가해서 처리하는 로직 --> 근데 이 경우 가게 검색 API 쪽에서 처리하나
			// 근데 만약 파티가 제대로 안 만들어졌을 시 가게도 추가 안된걸로 치려면 여기서 처리하고 트랜잭션 거는게 맞을듯.
			// TODO: 파티장도 INVITATION 만들어서 관리
			partyRepository.save(new Party(requestDto, hostUser, store));
		} else {
			partyRepository.save(new Party(requestDto, hostUser));
		}
	}

	// TODO: 정렬 기준 추가
	// TODO: 현재 인원 채워넣는 메소드는 파티 수락 메소드에 추가
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
		Party party = partyRepository.findById(partyId)
			.orElseThrow(() -> new CustomException(PARTY_NOT_FOUND));
		User host = userRepository.findById(userId)
			.orElseThrow(() -> new CustomException(USER_NOT_FOUND));

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
		Party party = partyRepository.findById(partyId)
			.orElseThrow(() -> new CustomException(PARTY_NOT_FOUND));

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
		if (requestDto.getStoreId() != null) {
			Store store = storeRepository.findById(requestDto.getStoreId())    // TODO: 이것도 검색 API로 추가해보고 안되는 걸로 변경
				.orElseThrow(() -> new CustomException(STORE_NOT_FOUND));
			party.update(requestDto, store);
		} else {
			party.update(requestDto);
		}
	}

	private boolean isHostOfParty(Party party, Long hostId) {
		return party.getHostUser().getId().equals(hostId);
	}
}
