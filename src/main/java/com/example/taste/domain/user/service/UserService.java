package com.example.taste.domain.user.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.taste.domain.favor.entity.Favor;
import com.example.taste.domain.favor.repository.FavorRepository;
import com.example.taste.domain.user.dto.request.UserDeleteRequestDto;
import com.example.taste.domain.user.dto.request.UserFavorUpdateListRequestDto;
import com.example.taste.domain.user.dto.request.UserFavorUpdateRequestDto;
import com.example.taste.domain.user.dto.request.UserUpdateRequestDto;
import com.example.taste.domain.user.dto.response.UserMyProfileResponseDto;
import com.example.taste.domain.user.dto.response.UserSimpleResponseDto;
import com.example.taste.domain.user.entity.Follow;
import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.entity.UserFavor;
import com.example.taste.domain.user.repository.FollowRepository;
import com.example.taste.domain.user.repository.UserFavorRepository;
import com.example.taste.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
	private final UserRepository userRepository;
	private final UserFavorRepository userFavorRepository;
	private final FavorRepository favorRepository;
	private final FollowRepository followRepository;
	//private final PasswordEncoder passwordEncoder;

	// 내 정보 조회
	public UserMyProfileResponseDto getMyProfile(Long userId) {
		User user = userRepository.findById(userId).orElseThrow();
		List<UserFavor> favorList = userFavorRepository.findAllByUser(userId);
		return new UserMyProfileResponseDto(user, favorList);
	}

	// 다른 유저 프로필 조회
	public UserMyProfileResponseDto getProfile(Long userId) {
		User user = userRepository.findById(userId).orElseThrow();
		List<UserFavor> favorList = userFavorRepository.findAllByUser(userId);
		return new UserMyProfileResponseDto(user, favorList);
	}

	// 유저 정보 업데이트
	@Transactional
	public void updateUser(Long userId, UserUpdateRequestDto requestDto) {
		User user = userRepository.findById(userId).orElseThrow();
		//	if (!passwordEncoder.matches(requestDto.getOldPassword(), user.getPassword())) {
		//		throw new CustomException(INVALID_PASSWORD);
		//	}
		//	requestDto.setNewPassword(passwordEncoder.encode(requestDto.getNewPassword()));
		user.update(requestDto);
	}

	// 유저 탈퇴
	@Transactional
	public void deleteUser(Long userId, UserDeleteRequestDto requestDto) {
		User user = userRepository.findById(userId).orElseThrow();
		//	if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
		//		throw new CustomException(INVALID_PASSWORD);
		//	}
		user.softDelete();
	}

	@Transactional
	public void updateUserFavor(Long userId, UserFavorUpdateListRequestDto requestDto) {
		List<UserFavorUpdateRequestDto> updateFavorList
			= requestDto.getUserFavorList();                            // 업데이트 요청 리스트

		User user = userRepository.findById(userId).orElseThrow();
		List<UserFavor> userFavorList = user.getUserFavorList();        // 기존 리스트

		// 1. 입맛 취향 업데이트 요청 리스트와 비교하여 기존의 항목은 유지
		// 2. 삭제된 항목, 혹은 3. 새로 추가한 항목을 반영 (수정 항목)

		// 삭제된 항목 반영
		ArrayList<UserFavor> newUserFavorList = userFavorList.stream()
			.filter(userFavor ->
				updateFavorList.stream()
					.anyMatch(updateItem -> isSameItem(updateItem, userFavor)))
			.collect(Collectors.toCollection(ArrayList::new));

		// 새로 추가할 항목만 추출
		List<UserFavorUpdateRequestDto> newUpdateFavorList = updateFavorList.stream()
			.filter(updateItem ->
				userFavorList.stream()
					.anyMatch(userFavor -> isSameItem(updateItem, userFavor))
			).toList();

		// Favor 리스트에 있는 것만 추가 반영(올바른 값만)
		// newUpdateFavorList 의 아이템 하나가 favorList 의 하나와 일치한다면 newUpdateFavorList 에 추가
		List<Favor> favorList = favorRepository.findAll();
		newUpdateFavorList.forEach(
			(updateItem) -> favorList.forEach(
				(favor) -> {
					if (isExistsItem(updateItem, favor)) {
						newUserFavorList.add(new UserFavor(user, favor));
					}
				})
		);

		user.setUserFavorList(newUserFavorList);
	}

	// 유저의 팔로잉 유저 목록 조회
	public List<UserSimpleResponseDto> getFollowingUserList(Long followerUserId) {
		return followRepository.findAllByFollower(followerUserId)
			.stream()
			.map(Follow::getFollowing)
			.map(UserSimpleResponseDto::new)
			.toList();
	}

	// 유저의 팔로워 유저 목록 조회
	public List<UserSimpleResponseDto> getFollowerUserList(Long followingUserId) {
		return followRepository.findAllByFollowing(followingUserId)
			.stream()
			.map(Follow::getFollower)
			.map(UserSimpleResponseDto::new)
			.toList();
	}

	@Transactional
	public void followUser(Long userId, Long followingUserId) {
		User user = userRepository.findById(userId).orElseThrow();
		User followingUser = userRepository.findById(followingUserId).orElseThrow();
		user.follow(user, followingUser);
		followingUser.followed();
	}

	@Transactional
	public void unfollowUser(Long followerUserId, Long followingUserId) {
		User follower = userRepository.findById(followerUserId).orElseThrow();
		User followingUser = userRepository.findById(followingUserId).orElseThrow();
		Follow follow = followRepository.findByFollowerAndFollower(followerUserId, followingUserId);

		follower.unfollow(follow);
		followingUser.unfollowed();
	}

	private boolean isSameItem(UserFavorUpdateRequestDto update, UserFavor favor) {
		return update.getUserFavorId().equals(favor.getId()) &&
			update.getName().equals(favor.getFavor().getName());
	}

	private boolean isExistsItem(UserFavorUpdateRequestDto update, Favor favor) {
		return update.getUserFavorId().equals(favor.getId()) &&
			update.getName().equals(favor.getName());
	}
}
