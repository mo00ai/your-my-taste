package com.example.taste.domain.match.service;

import static com.example.taste.domain.match.exception.MatchErrorCode.USER_MATCH_COND_NOT_FOUND;
import static com.example.taste.domain.store.exception.StoreErrorCode.CATEGORY_NOT_FOUND;
import static com.example.taste.domain.store.exception.StoreErrorCode.STORE_NOT_FOUND;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.taste.common.exception.CustomException;
import com.example.taste.domain.match.dto.request.UserMatchCondCreateRequestDto;
import com.example.taste.domain.match.dto.response.UserMatchCondResponseDto;
import com.example.taste.domain.match.entity.UserMatchCond;
import com.example.taste.domain.match.entity.UserMatchCondCategory;
import com.example.taste.domain.match.entity.UserMatchCondStore;
import com.example.taste.domain.match.repository.UserMatchCondCategoryRepository;
import com.example.taste.domain.match.repository.UserMatchCondRepository;
import com.example.taste.domain.match.repository.UserMatchCondStoreRepository;
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
	private final UserMatchCondStoreRepository userMatchCondStoreRepository;
	private final UserMatchCondCategoryRepository userMatchCondCategoryRepository;

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

		// 맛집 리스트 변환
		List<Store> storeList = null;
		if (requestDto.getStores() != null) {
			storeList = storeRepository.findAllById(requestDto.getStores());

			if (storeList.size() != requestDto.getStores().size()) {
				throw new CustomException(STORE_NOT_FOUND);
			}

			matchCond.setStores(storeList.stream()
				.map((s) -> new UserMatchCondStore(matchCond, s)).toList());
		}

		// 카테고리 리스트 변환
		List<Category> categoryList = null;
		if (requestDto.getCategories() != null) {
			categoryList = categoryRepository.findAllByNameIn(requestDto.getCategories());

			if (categoryList.size() != requestDto.getCategories().size()) {
				throw new CustomException(CATEGORY_NOT_FOUND);
			}

			matchCond.setCategories(categoryList.stream()
				.map((c) -> new UserMatchCondCategory(matchCond, c)).toList());
		}
	}

	public List<UserMatchCondResponseDto> findUserMatchCond(Long userId) {
		User user = entityFetcher.getUserOrThrow(userId);
		return userMatchCondRepository.findAllByUser(user).stream()
			.map(UserMatchCondResponseDto::new)
			.toList();
	}
}
