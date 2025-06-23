package com.example.taste.domain.match.service;

import static com.example.taste.domain.favor.exception.FavorErrorCode.NOT_FOUND_FAVOR;
import static com.example.taste.domain.match.exception.MatchErrorCode.ACTIVE_MATCH_EXISTS;
import static com.example.taste.domain.match.exception.MatchErrorCode.FORBIDDEN_USER_MATCH_INFO;
import static com.example.taste.domain.store.exception.StoreErrorCode.CATEGORY_NOT_FOUND;
import static com.example.taste.domain.store.exception.StoreErrorCode.STORE_NOT_FOUND;
import static com.example.taste.domain.user.exception.UserErrorCode.NOT_FOUND_USER;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.taste.common.exception.CustomException;
import com.example.taste.common.util.EntityFetcher;
import com.example.taste.domain.favor.entity.Favor;
import com.example.taste.domain.favor.repository.FavorRepository;
import com.example.taste.domain.match.dto.request.UserMatchInfoCreateRequestDto;
import com.example.taste.domain.match.dto.request.UserMatchInfoUpdateRequestDto;
import com.example.taste.domain.match.dto.response.UserMatchInfoResponseDto;
import com.example.taste.domain.match.entity.UserMatchInfo;
import com.example.taste.domain.match.entity.UserMatchInfoCategory;
import com.example.taste.domain.match.entity.UserMatchInfoFavor;
import com.example.taste.domain.match.entity.UserMatchInfoStore;
import com.example.taste.domain.match.repository.UserMatchInfoRepository;
import com.example.taste.domain.party.enums.MatchStatus;
import com.example.taste.domain.store.entity.Category;
import com.example.taste.domain.store.entity.Store;
import com.example.taste.domain.store.repository.CategoryRepository;
import com.example.taste.domain.store.repository.StoreRepository;
import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class MatchInfoService {
	private final EntityFetcher entityFetcher;
	private final UserRepository userRepository;
	private final StoreRepository storeRepository;
	private final CategoryRepository categoryRepository;
	private final FavorRepository favorRepository;
	private final UserMatchInfoRepository userMatchInfoRepository;

	// MEMO : 맛집, 카테고리, 지역 중 특정 조합만 허용할 건지?
	@Transactional
	public void createUserMatchInfo(Long userId, UserMatchInfoCreateRequestDto requestDto) {
		User user = entityFetcher.getUserOrThrow(userId);
		UserMatchInfo userMatchInfo = userMatchInfoRepository.save(
			new UserMatchInfo(requestDto, user));

		// 맛집 리스트 세팅
		if (requestDto.getStoreList() != null) {
			userMatchInfo.updateStoreList(getValidUserMatchInfoStores(requestDto.getStoreList(), userMatchInfo));
		}

		// 카테고리 리스트 세팅
		if (requestDto.getCategoryList() != null) {
			userMatchInfo.updateCategoryList(
				getValidUserMatchInfoCategories(requestDto.getCategoryList(), userMatchInfo));
		}

		// 입맛 리스트 세팅
		if (requestDto.getStoreList() != null) {
			userMatchInfo.updateFavorList(getValidUserMatchInfoFavors(requestDto.getFavorList(), userMatchInfo));
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
		Long userId, Long userMatchInfoId, UserMatchInfoUpdateRequestDto requestDto) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new CustomException(NOT_FOUND_USER));
		UserMatchInfo matchInfo = entityFetcher.getUserMatchInfoOrThrow(userMatchInfoId);
		// 자신의 소유가 아닌 경우
		if (!matchInfo.isOwner(user)) {
			throw new CustomException(FORBIDDEN_USER_MATCH_INFO);
		}

		// 매칭 중이면 업데이트 불가
		if (!matchInfo.isStatus(MatchStatus.IDLE)) {
			throw new CustomException(ACTIVE_MATCH_EXISTS);
		}

		matchInfo.update(requestDto);

		// 맛집 리스트 세팅
		if (requestDto.getStoreList() != null) {
			matchInfo.updateStoreList(getValidUserMatchInfoStores(requestDto.getStoreList(), matchInfo));
		}

		// 카테고리 리스트 세팅
		if (requestDto.getCategoryList() != null) {
			matchInfo.updateCategoryList(getValidUserMatchInfoCategories(requestDto.getCategoryList(), matchInfo));
		}

		// 입맛 리스트 세팅
		if (requestDto.getStoreList() != null) {
			matchInfo.updateFavorList(getValidUserMatchInfoFavors(requestDto.getFavorList(), matchInfo));
		}
	}

	@Transactional
	public void deleteUserMatchInfo(Long userId, Long matchInfoId) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new CustomException(NOT_FOUND_USER));
		UserMatchInfo matchInfo = entityFetcher.getUserMatchInfoOrThrow(matchInfoId);
		// 자신의 소유가 아닌 경우
		if (!matchInfo.isOwner(user)) {
			throw new CustomException(FORBIDDEN_USER_MATCH_INFO);
		}

		// 매칭 중이면 삭제 불가
		if (!matchInfo.isStatus(MatchStatus.IDLE)) {
			throw new CustomException(ACTIVE_MATCH_EXISTS);
		}

		userMatchInfoRepository.deleteById(matchInfoId);
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

	private List<UserMatchInfoFavor> getValidUserMatchInfoFavors(
		List<String> favorNameList, UserMatchInfo matchInfo) {
		List<Favor> favorList = favorRepository.findAllByNameIn(favorNameList);

		if (favorList.size() != favorNameList.size()) {
			throw new CustomException(NOT_FOUND_FAVOR);
		}

		return favorList.stream()
			.map((f) -> new UserMatchInfoFavor(matchInfo, f)).toList();
	}
}
