package com.example.taste.domain.party.service;

import static com.example.taste.domain.store.exception.StoreErrorCode.STORE_NOT_FOUND;
import static com.example.taste.domain.user.exception.UserErrorCode.USER_NOT_FOUND;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import com.example.taste.common.exception.CustomException;
import com.example.taste.domain.party.dto.request.PartyCreateRequestDto;
import com.example.taste.domain.party.entity.Party;
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

	public void createParty(Long hostId, PartyCreateRequestDto requestDto) {
		User hostUser = userRepository.findById(hostId)
			.orElseThrow(() -> new CustomException(USER_NOT_FOUND));
		if (requestDto.getStoreId() != null) {
			Store store = storeRepository.findById(requestDto.getStoreId())
				.orElseThrow(() -> new CustomException(STORE_NOT_FOUND));
			// TODO: store 에 없을 시 새로 추가해서 처리하는 로직 --> 근데 이 경우 가게 검색 API 쪽에서 처리하나/
			partyRepository.save(new Party(requestDto, hostUser, store));
		} else {
			partyRepository.save(new Party(requestDto, hostUser));
		}
	}
}
