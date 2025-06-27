package com.example.taste.domain.user.service;

import static com.example.taste.domain.user.exception.UserErrorCode.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.jooq.Cursor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.jooq.tables.records.UsersRecord;
import com.example.taste.common.exception.CustomException;
import com.example.taste.domain.favor.entity.Favor;
import com.example.taste.domain.favor.repository.FavorRepository;
import com.example.taste.domain.pk.entity.PkLog;
import com.example.taste.domain.pk.enums.PkType;
import com.example.taste.domain.pk.repository.PkLogRepository;
import com.example.taste.domain.store.repository.StoreBucketRepository;
import com.example.taste.domain.user.dto.request.UserDeleteRequestDto;
import com.example.taste.domain.user.dto.request.UserFavorUpdateRequestDto;
import com.example.taste.domain.user.dto.response.UserMyProfileResponseDto;
import com.example.taste.domain.user.dto.response.UserProfileResponseDto;
import com.example.taste.domain.user.dto.response.UserSimpleResponseDto;
import com.example.taste.domain.user.entity.Follow;
import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.entity.UserFavor;
import com.example.taste.domain.user.repository.FollowRepository;
import com.example.taste.domain.user.repository.UserFavorRepository;
import com.example.taste.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
	private final UserRepository userRepository;
	private final UserFavorRepository userFavorRepository;
	private final FavorRepository favorRepository;
	private final FollowRepository followRepository;
	private final PasswordEncoder passwordEncoder;
	private final StoreBucketRepository storeBucketRepository;
	private final PkLogRepository pkLogRepository;

	// 내 정보 조회
	public UserMyProfileResponseDto getMyProfile(Long userId) {
		User user = userRepository.findByIdWithUserFavorList(userId)
			.orElseThrow(() -> new CustomException(NOT_FOUND_USER));
		return new UserMyProfileResponseDto(user);
	}

	// 다른 유저 프로필 조회
	public UserProfileResponseDto getProfile(Long userId) {
		User user = userRepository.findByIdWithUserFavorList(userId)
			.orElseThrow(() -> new CustomException(NOT_FOUND_USER));
		return new UserProfileResponseDto(user);
	}

	// 유저 탈퇴
	@Transactional
	public void deleteUser(Long userId, UserDeleteRequestDto requestDto) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new CustomException(NOT_FOUND_USER));
		if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
			throw new CustomException(INVALID_PASSWORD);
		}
		storeBucketRepository.deleteAllByUser(user);
		user.softDelete();
	}

	@Transactional
	public void updateUserFavors(Long userId, List<UserFavorUpdateRequestDto> requestDtoList) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new CustomException(NOT_FOUND_USER));
		List<UserFavor> userFavorList = user.getUserFavorList();        // 기존 리스트
		List<Favor> favorList = favorRepository.findAll();                // 유효한 맵 리스트

		// 입맛 취향 업데이트 요청 리스트와 비교하여 기존의 항목은 유지
		// 1. 삭제된 항목을 리스트에서 제거, 2. 새로 추가한 항목을 반영 (수정 항목)

		// 기존 항목 맵 생성
		Map<String, UserFavor> existingFavorMap = userFavorList.stream()
			.collect(Collectors.toMap(uf -> uf.getFavor().getName(), uf -> uf));

		// 유효한 Favor 맵 생성
		Map<String, Favor> validFavorMap = favorList.stream()
			.collect(Collectors.toMap(Favor::getName, f -> f));

		// 요청 리스트 Favor 맵 생성
		Map<String, UserFavorUpdateRequestDto> favorRequestMap = requestDtoList.stream()
			.collect(Collectors.toMap(UserFavorUpdateRequestDto::getName, ufr -> ufr));

		// 1. 삭제된 항목 리스트에서 제거
		List<UserFavor> removeList = existingFavorMap.entrySet().stream()
			.filter(e -> !favorRequestMap.containsKey(e.getKey()))
			.map(Map.Entry::getValue)
			.toList();
		user.removeUserFavorList(removeList);

		// 2. 새로 추가할 항목만 추출 (기존 리스트가 비었다면 추가/수정 요청 리스트 그대로 사용, 아니면 필터링)
		// Favor 리스트에 있는 것만 추가 (올바른 값만)
		// updateFavorList 의 아이템 하나가 favorList 의 하나와 일치한다면 새로운 userFavor 생성
		List<UserFavor> updateList = favorRequestMap.keySet().stream()
			.filter(key -> !existingFavorMap.containsKey(key))
			.map(validFavorMap::get)
			.filter(Objects::nonNull)
			.map(favor -> new UserFavor(user, favor))
			.toList();

		// 최종 저장
		userFavorRepository.saveAll(updateList);
	}

	// 유저의 팔로잉 유저 목록 조회
	public List<UserSimpleResponseDto> getFollowingUserList(Long followerUserId) {
		return followRepository.findByFollowerId(followerUserId)
			.stream()
			.map(Follow::getFollowing)
			.map(UserSimpleResponseDto::new)
			.toList();
	}

	// 유저의 팔로워 유저 목록 조회
	public List<UserSimpleResponseDto> getFollowerUserList(Long followingUserId) {
		return followRepository.findByFollowingId(followingUserId)
			.stream()
			.map(Follow::getFollower)
			.map(UserSimpleResponseDto::new)
			.toList();
	}

	@Transactional
	public void followUser(Long userId, Long followingUserId) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new CustomException(NOT_FOUND_USER));
		User followingUser = userRepository.findById(followingUserId)
			.orElseThrow(() -> new CustomException(NOT_FOUND_USER));
		if (followRepository.existsByFollowerIdAndFollowingId(userId, followingUserId)) {
			throw new CustomException(ALREADY_FOLLOWED);
		}
		Follow follow = followRepository.save(new Follow(user, followingUser));
		user.follow(follow);
		followingUser.followed();
	}

	@Transactional
	public void unfollowUser(Long followerUserId, Long followingUserId) {
		User follower = userRepository.findById(followerUserId)
			.orElseThrow(() -> new CustomException(NOT_FOUND_USER));
		User followingUser = userRepository.findById(followingUserId)
			.orElseThrow(() -> new CustomException(NOT_FOUND_USER));
		Follow follow = followRepository.findByFollowerIdAndFollowingId(followerUserId, followingUserId)
			.orElseThrow(() -> new CustomException(FOLLOW_NOT_FOUND));

		follower.unfollow(follow);
		followingUser.unfollowed();
	}

	@Transactional
	public void increaseUserPoint(User user, int point) {
		user.increasePoint(point);
	}

	@Transactional(readOnly = true)
	public List<User> findPkRankingUsers() {
		return userRepository.findAllByOrderByPointDesc(PageRequest.of(0, 100));
	}

	// @Transactional
	// public void resetUsersPoint() {
	//
	// 	List<User> usersWithPoints = userRepository.findByPointGreaterThan(0);
	//
	// 	if (!usersWithPoints.isEmpty()) {
	//
	// 		List<PkLog> resetLogs = usersWithPoints.stream()
	// 			.map(user -> PkLog.builder()
	// 				.pkType(PkType.RESET)
	// 				.point(0)
	// 				.user(user)
	// 				.createdAt(LocalDateTime.now())
	// 				.build())
	// 			.toList();
	//
	// 		//jdbc
	// 		// pkLogJdbcRepository.batchInsert(resetLogs);
	//
	// 		//jpa
	// 		// pkLogRepository.saveAll(resetLogs);
	//
	// 		//jooq
	// 		pkLogRepository.insertPkLogs(resetLogs);
	//
	// 	}
	//
	// 	//jdbc
	// 	// userJdbcRepository.resetAllUserPoints();
	//
	// 	//jpa
	// 	// userRepository.resetAllPoints();
	//
	// 	//jooq
	// 	userRepository.resetAllUserPoints();
	//
	// }

	@Transactional
	public void resetUsersPoint() {

		int batchSize = 1000;
		List<PkLog> logs = new ArrayList<>();

		try (Cursor<UsersRecord> cursor = userRepository.findByPointWithJooqCursor(0)) {

			while (cursor.hasNext()) {
				UsersRecord jooqUser = cursor.fetchNext();

				// User user = User.ofId(jooqUser.getId());

				//가짜 객체
				User user = userRepository.getReferenceById(jooqUser.getId());

				logs.add(PkLog.builder()
					.pkType(PkType.RESET)
					.point(0)
					.user(user)
					.createdAt(LocalDateTime.now())
					.build());
			}

			if (!logs.isEmpty()) {
				for (int i = 0; i < logs.size(); i += batchSize) {
					int end = Math.min(i + batchSize, logs.size());
					List<PkLog> batch = logs.subList(i, end);
					pkLogRepository.insertPkLogs(batch);
				}
			}
		}

		// 전체 포인트 일괄 초기화
		userRepository.resetAllUserPoints();
	}

	@Transactional
	public long resetPostingCnt() {
		return userRepository.resetPostingCnt();
	}
}
