package com.example.taste.domain.match.service;

import static com.example.taste.domain.match.exception.MatchErrorCode.ACTIVE_MATCH_EXISTS;
import static com.example.taste.domain.match.exception.MatchErrorCode.USER_MATCH_COND_NOT_FOUND;
import static com.example.taste.domain.store.exception.StoreErrorCode.CATEGORY_NOT_FOUND;
import static com.example.taste.domain.store.exception.StoreErrorCode.STORE_NOT_FOUND;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.taste.common.exception.CustomException;
import com.example.taste.domain.match.dto.request.UserMatchCondCreateRequestDto;
import com.example.taste.domain.match.dto.request.UserMatchCondUpdateRequestDto;
import com.example.taste.domain.match.dto.response.UserMatchCondResponseDto;
import com.example.taste.domain.match.entity.UserMatchCond;
import com.example.taste.domain.match.entity.UserMatchCondCategory;
import com.example.taste.domain.match.entity.UserMatchCondStore;
import com.example.taste.domain.match.repository.UserMatchCondRepository;
import com.example.taste.domain.party.enums.MatchingStatus;
import com.example.taste.domain.store.entity.Category;
import com.example.taste.domain.store.entity.Store;
import com.example.taste.domain.store.repository.CategoryRepository;
import com.example.taste.domain.store.repository.StoreRepository;
import com.example.taste.domain.user.entity.User;

@Service
@RequiredArgsConstructor
public class MatchService {
	private final EntityFetcher entityFetcher;
	private final StoreRepository storeRepository;
	private final CategoryRepository categoryRepository;
	private final UserMatchCondRepository userMatchCondRepository;

	public void startUserMatch(Long conditionId) {
		UserMatchCond userMatchCond = userMatchCondRepository.findById(conditionId)
			.orElseThrow(() -> new CustomException(USER_MATCH_COND_NOT_FOUND));
		// TODO: 매칭 시작, 매칭 시작일 알고리즘 추가
	}

	// MEMO : 맛집, 카테고리, 지역 중 특정 조합만 허용할 건지?
	@Transactional
	public void createUserMatchCond(Long userId, UserMatchCondCreateRequestDto requestDto) {
		User user = entityFetcher.getUserOrElseThrow(userId);
		UserMatchCond matchCond = userMatchCondRepository.save(
			new UserMatchCond(requestDto, user));

		// 맛집 리스트 세팅
		if (requestDto.getStores() != null) {
			matchCond.setStores(getValidCondStores(requestDto.getStores(), matchCond));
		}

		// 카테고리 리스트 세팅
		if (requestDto.getCategories() != null) {
			matchCond.setCategories(getValidCondCategories(requestDto.getCategories(), matchCond));
		}
	}

	public List<UserMatchCondResponseDto> findUserMatchCond(Long userId) {
		User user = entityFetcher.getUserOrThrow(userId);
		return userMatchCondRepository.findAllByUser(user).stream()
			.map(UserMatchCondResponseDto::new)
			.toList();
	}

	@Transactional
	public void updateUserMatchCond(
		Long matchingConditionId, UserMatchCondUpdateRequestDto requestDto) {
		UserMatchCond matchCond = userMatchCondRepository.findById(matchingConditionId)
			.orElseThrow(() -> new CustomException(USER_MATCH_COND_NOT_FOUND));        // TODO: entity fetcher

		// 매칭 중이면 업데이트 불가
		if (!matchCond.getMatchingStatus().equals(MatchingStatus.IDLE)) {
			throw new CustomException(ACTIVE_MATCH_EXISTS);
		}

		matchCond.update(requestDto);

		// 맛집 리스트 세팅
		if (requestDto.getStores() != null) {
			matchCond.setStores(getValidCondStores(requestDto.getStores(), matchCond));
		}

		// 카테고리 리스트 세팅
		if (requestDto.getCategories() != null) {
			matchCond.setCategories(getValidCondCategories(requestDto.getCategories(), matchCond));
		}
	}

	private List<UserMatchCondStore> getValidCondStores(
		List<Long> storeIdList, UserMatchCond matchCond) {
		List<Store> storeList = storeRepository.findAllById(storeIdList);

		if (storeList.size() != storeIdList.size()) {
			throw new CustomException(STORE_NOT_FOUND);
		}

		return storeList.stream()
			.map((s) -> new UserMatchCondStore(matchCond, s)).toList();
	}

	private List<UserMatchCondCategory> getValidCondCategories(
		List<String> categoryNameList, UserMatchCond matchCond) {
		List<Category> categoryList = categoryRepository.findAllByNameIn(categoryNameList);

		if (categoryList.size() != categoryNameList.size()) {
			throw new CustomException(CATEGORY_NOT_FOUND);
		}

		return categoryList.stream()
			.map((c) -> new UserMatchCondCategory(matchCond, c)).toList();
	}

	@Transactional
	public void deleteUserMatchCond(Long matchingConditionId) {
		MatchingStatus matchingStatus = userMatchCondRepository.findUserMatchCondById(matchingConditionId);

		// 매칭 중이면 업데이트 불가
		if (!matchingStatus.equals(MatchingStatus.IDLE)) {
			throw new CustomException(ACTIVE_MATCH_EXISTS);
		}

		userMatchCondRepository.deleteById(matchingConditionId);
	}
}
