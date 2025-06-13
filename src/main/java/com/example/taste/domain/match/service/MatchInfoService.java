package com.example.taste.domain.match.service;

import static com.example.taste.domain.match.exception.MatchErrorCode.ACTIVE_MATCH_EXISTS;
import static com.example.taste.domain.store.exception.StoreErrorCode.CATEGORY_NOT_FOUND;
import static com.example.taste.domain.store.exception.StoreErrorCode.STORE_NOT_FOUND;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.taste.common.exception.CustomException;
import com.example.taste.common.util.EntityFetcher;
import com.example.taste.domain.match.dto.request.UserMatchInfoCreateRequestDto;
import com.example.taste.domain.match.dto.request.UserMatchInfoUpdateRequestDto;
import com.example.taste.domain.match.dto.response.UserMatchInfoResponseDto;
import com.example.taste.domain.match.entity.UserMatchInfo;
import com.example.taste.domain.match.entity.UserMatchInfoCategory;
import com.example.taste.domain.match.entity.UserMatchInfoStore;
import com.example.taste.domain.match.repository.UserMatchInfoRepository;
import com.example.taste.domain.party.enums.MatchStatus;
import com.example.taste.domain.store.entity.Category;
import com.example.taste.domain.store.entity.Store;
import com.example.taste.domain.store.repository.CategoryRepository;
import com.example.taste.domain.store.repository.StoreRepository;
import com.example.taste.domain.user.entity.User;

@Service
@RequiredArgsConstructor
public class MatchInfoService {
	private final EntityFetcher entityFetcher;
	private final StoreRepository storeRepository;
	private final CategoryRepository categoryRepository;
	private final UserMatchInfoRepository userMatchInfoRepository;

	// MEMO : 맛집, 카테고리, 지역 중 특정 조합만 허용할 건지?
	@Transactional
	public void createUserMatchInfo(Long userId, UserMatchInfoCreateRequestDto requestDto) {
		User user = entityFetcher.getUserOrThrow(userId);
		UserMatchInfo userMatchInfo = userMatchInfoRepository.save(
			new UserMatchInfo(requestDto, user));

		// 맛집 리스트 세팅
		if (requestDto.getStores() != null) {
			userMatchInfo.setStores(getValidUserMatchInfoStores(requestDto.getStores(), userMatchInfo));
		}

		// 카테고리 리스트 세팅
		if (requestDto.getCategories() != null) {
			userMatchInfo.setCategories(getValidUserMatchInfoCategories(requestDto.getCategories(), userMatchInfo));
		}
	}

	public List<UserMatchInfoResponseDto> findUserMatchInfo(Long userId) {
		User user = entityFetcher.getUserOrThrow(userId);
		return userMatchInfoRepository.findAllByUser(user).stream()
			.map(UserMatchInfoResponseDto::new)
			.toList();
	}

	@Transactional
	public void updateUserMatchInfo(
		Long userMatchInfoId, UserMatchInfoUpdateRequestDto requestDto) {
		UserMatchInfo matchInfo = entityFetcher.getUserMatchInfoOrThrow(userMatchInfoId);

		// 매칭 중이면 업데이트 불가
		if (!matchInfo.getMatchStatus().equals(MatchStatus.IDLE)) {
			throw new CustomException(ACTIVE_MATCH_EXISTS);
		}

		matchInfo.update(requestDto);

		// 맛집 리스트 세팅
		if (requestDto.getStores() != null) {
			matchInfo.setStores(getValidUserMatchInfoStores(requestDto.getStores(), matchInfo));
		}

		// 카테고리 리스트 세팅
		if (requestDto.getCategories() != null) {
			matchInfo.setCategories(getValidUserMatchInfoCategories(requestDto.getCategories(), matchInfo));
		}
	}

	@Transactional
	public void deleteUserMatchInfo(Long matchingConditionId) {
		MatchStatus matchStatus = userMatchInfoRepository.findUserMatchStatusById(matchingConditionId);

		// 매칭 중이면 삭제 불가
		if (!matchStatus.equals(MatchStatus.IDLE)) {
			throw new CustomException(ACTIVE_MATCH_EXISTS);
		}

		userMatchInfoRepository.deleteById(matchingConditionId);
	}

	private List<UserMatchInfoStore> getValidUserMatchInfoStores(
		List<Long> storeIdList, UserMatchInfo matchInfo) {
		List<Store> storeList = storeRepository.findAllById(storeIdList);

		if (storeList.size() != storeIdList.size()) {
			throw new CustomException(STORE_NOT_FOUND);
		}

		return storeList.stream()
			.map((s) -> new UserMatchInfoStore(matchInfo, s)).toList();
	}

	private List<UserMatchInfoCategory> getValidUserMatchInfoCategories(
		List<String> categoryNameList, UserMatchInfo matchInfo) {
		List<Category> categoryList = categoryRepository.findAllByNameIn(categoryNameList);

		if (categoryList.size() != categoryNameList.size()) {
			throw new CustomException(CATEGORY_NOT_FOUND);
		}

		return categoryList.stream()
			.map((c) -> new UserMatchInfoCategory(matchInfo, c)).toList();
	}
}
