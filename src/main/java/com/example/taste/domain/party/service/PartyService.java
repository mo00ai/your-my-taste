package com.example.taste.domain.party.service;

import static com.example.taste.domain.store.exception.StoreErrorCode.STORE_NOT_FOUND;
import static com.example.taste.domain.user.exception.UserErrorCode.USER_NOT_FOUND;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.taste.common.exception.CustomException;
import com.example.taste.domain.party.dto.reponse.PartyResponseDto;
import com.example.taste.domain.party.dto.request.PartyCreateRequestDto;
import com.example.taste.domain.party.entity.Party;
import com.example.taste.domain.party.enums.PartyFilter;
import com.example.taste.domain.party.repository.PartyRepository;
import com.example.taste.domain.store.entity.Store;
import com.example.taste.domain.store.repository.StoreRepository;
import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class PartyService {
	private final UserRepository userRepository;
	private final StoreRepository storeRepository;
	private final PartyRepository partyRepository;

	@Transactional
	public void createParty(Long hostId, PartyCreateRequestDto requestDto) {
		User hostUser = userRepository.findById(hostId)
			.orElseThrow(() -> new CustomException(USER_NOT_FOUND));
		if (requestDto.getStoreId() != null) {
			Store store = storeRepository.findById(requestDto.getStoreId())
				.orElseThrow(() -> new CustomException(STORE_NOT_FOUND));
			// TODO: store 에 없을 시 새로 추가해서 처리하는 로직 --> 근데 이 경우 가게 검색 API 쪽에서 처리하나
			// 근데 만약 파티가 제대로 안 만들어졌을 시 가게도 추가 안된걸로 치려면 여기서 처리하고 트랜잭션 거는게 맞을듯.
			partyRepository.save(new Party(requestDto, hostUser, store));
		} else {
			partyRepository.save(new Party(requestDto, hostUser));
		}
	}

	// TODO: 정렬 기준 추가
	public List<PartyResponseDto> getParties(Long userId, String filter) {
		PartyFilter partyFilter = PartyFilter.of(filter);

		switch (partyFilter) {
			case ALL:
				// 유저가 열고 있는 파티 제외하고 모든 파티 보여줌
				return partyRepository.findAllByUserNot(userId).stream()
					.map(PartyResponseDto::new)
					.toList();
			case MY:
				// 유저가 참가, 호스트인 파티 모두 보여줌
				return partyRepository.findAllByUserIn(userId).stream()
					.map(PartyResponseDto::new)
					.toList();
			default:
				return partyRepository.findAllByUserNot(userId).stream()
					.map(PartyResponseDto::new)
					.toList();
		}
	}
}
