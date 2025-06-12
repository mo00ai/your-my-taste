package com.example.taste.domain.user.service;

import static com.example.taste.domain.user.exception.UserErrorCode.INVALID_PASSWORD;
import static com.example.taste.domain.user.exception.UserErrorCode.USER_NOT_FOUND;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.taste.common.exception.CustomException;
import com.example.taste.common.util.EntityFetcher;
import com.example.taste.domain.favor.entity.Favor;
import com.example.taste.domain.favor.repository.FavorRepository;
import com.example.taste.domain.image.entity.Image;
import com.example.taste.domain.image.enums.ImageType;
import com.example.taste.domain.image.service.ImageService;
import com.example.taste.domain.pk.entity.PkLog;
import com.example.taste.domain.pk.enums.PkType;
import com.example.taste.domain.pk.repository.PkLogJdbcRepository;
import com.example.taste.domain.user.dto.request.UserDeleteRequestDto;
import com.example.taste.domain.user.dto.request.UserFavorUpdateRequestDto;
import com.example.taste.domain.user.dto.request.UserUpdateRequestDto;
import com.example.taste.domain.user.dto.response.UserMyProfileResponseDto;
import com.example.taste.domain.user.dto.response.UserProfileResponseDto;
import com.example.taste.domain.user.dto.response.UserSimpleResponseDto;
import com.example.taste.domain.user.entity.Follow;
import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.entity.UserFavor;
import com.example.taste.domain.user.repository.FollowRepository;
import com.example.taste.domain.user.repository.UserFavorRepository;
import com.example.taste.domain.user.repository.UserJdbcRepository;
import com.example.taste.domain.user.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
	private final EntityFetcher entityFetcher;
	private final ImageService imageService;
	private final UserRepository userRepository;
	private final UserFavorRepository userFavorRepository;
	private final FavorRepository favorRepository;
	private final FollowRepository followRepository;
	private final PasswordEncoder passwordEncoder;
	private final UserJdbcRepository userJdbcRepository;
	private final PkLogJdbcRepository pkLogJdbcRepository;

	// 내 정보 조회
	public UserMyProfileResponseDto getMyProfile(Long userId) {
		User user = userRepository.findByIdWithUserFavorList(userId)
			.orElseThrow(() -> new CustomException(USER_NOT_FOUND));
		return new UserMyProfileResponseDto(user);
	}

	// 다른 유저 프로필 조회
	public UserProfileResponseDto getProfile(Long userId) {
		User user = userRepository.findByIdWithUserFavorList(userId)
			.orElseThrow(() -> new CustomException(USER_NOT_FOUND));
		return new UserProfileResponseDto(user);
	}

	// 유저 정보 업데이트
	@Transactional
	public void updateUser(Long userId, UserUpdateRequestDto requestDto, MultipartFile file) {
		User user = entityFetcher.getUserOrThrow(userId);
		if (!passwordEncoder.matches(requestDto.getOldPassword(), user.getPassword())) {
			throw new CustomException(INVALID_PASSWORD);
		}
		requestDto.setNewPassword(passwordEncoder.encode(requestDto.getNewPassword()));
		user.update(requestDto);

		// 프로필 이미지 저장
		if (file != null) {
			Image oldImage = user.getImage();

			if (oldImage != null) {
				try {
					imageService.update(oldImage.getId(), ImageType.USER, file);
				} catch (IOException e) {    // 이미지 저장 실패하더라도 정보 업데이트 진행 // TODO: 이미지 트랜잭션 확인 필요
					log.info("유저 이미지 업데이트에 실패하였습니다 (email: " + user.getEmail() + ")");
				}
			} else {
				try {
					Image image = imageService.saveImage(file, ImageType.USER);
					user.setImage(image);
				} catch (IOException e) {    // 이미지 저장 실패하더라도 정보 업데이트 진행 // TODO: 이미지 트랜잭션 확인 필요
					log.info("유저 이미지 저장에 실패하였습니다 (email: " + user.getEmail() + ")");
				}
			}
		}
	}

	// 유저 탈퇴
	@Transactional
	public void deleteUser(Long userId, UserDeleteRequestDto requestDto) {
		User user = entityFetcher.getUserOrThrow(userId);
		if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
			throw new CustomException(INVALID_PASSWORD);
		}
		user.softDelete();
	}

	@Transactional
	public void updateUserFavors(Long userId, List<UserFavorUpdateRequestDto> requestDtoList) {
		User user = entityFetcher.getUserOrThrow(userId);
		List<UserFavor> userFavorList = user.getUserFavorList();        // 기존 리스트

		// 1. 입맛 취향 업데이트 요청 리스트와 비교하여 기존의 항목은 유지
		// 2. 삭제된 항목을 리스트에서 제거, 3. 새로 추가한 항목을 반영 (수정 항목)

		// 삭제된 항목 리스트에서 제거
		user.removeUserFavorList(userFavorList.stream()
			.filter(userFavor ->
				requestDtoList.stream()
					.noneMatch(updateItem -> isSameItem(updateItem, userFavor)))
			.collect(Collectors.toCollection(ArrayList::new)));

		// 새로 추가할 항목만 추출 (기존 리스트가 비었다면 추가/수정 요청 리스트 그대로 사용, 아니면 필터링)
		List<UserFavorUpdateRequestDto> updateFavorList =
			userFavorList == null || userFavorList.isEmpty() ? requestDtoList
				: requestDtoList.stream()
				.filter(updateItem ->
					userFavorList.stream()
						.noneMatch(userFavor -> isSameItem(updateItem, userFavor))
				).toList();

		// Favor 리스트에 있는 것만 추가 (올바른 값만)
		// updateFavorList 의 아이템 하나가 favorList 의 하나와 일치한다면 새로운 userFavor 생성
		List<Favor> favorList = favorRepository.findAll();
		ArrayList<UserFavor> updateUserFavorList = new ArrayList<>();

		updateFavorList.forEach(
			(updateItem) -> favorList.forEach(
				(favor) -> {
					if (isExistsItem(updateItem, favor)) {
						updateUserFavorList.add(new UserFavor(user, favor));
					}
				})
		);

		// 최종 저장
		userFavorRepository.saveAll(updateUserFavorList);
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
		User user = entityFetcher.getUserOrThrow(userId);
		User followingUser = entityFetcher.getUserOrThrow(followingUserId);
		user.follow(user, followingUser);
		followingUser.followed();
	}

	@Transactional
	public void unfollowUser(Long followerUserId, Long followingUserId) {
		User follower = entityFetcher.getUserOrThrow(followerUserId);
		User followingUser = entityFetcher.getUserOrThrow(followingUserId);
		Follow follow = followRepository.findByFollowerAndFollower(followerUserId, followingUserId);

		follower.unfollow(follow);
		followingUser.unfollowed();
	}

	private boolean isSameItem(UserFavorUpdateRequestDto update, UserFavor favor) {
		if (update.getUserFavorId() != null) {
			return Objects.equals(update.getUserFavorId(), favor.getId());
		}
		return update.getName().equals(favor.getFavor().getName());
	}

	private boolean isExistsItem(UserFavorUpdateRequestDto update, Favor favor) {
		return update.getName().equals(favor.getName());
	}

	@Transactional
	public void increaseUserPoint(User user, int point) {
		user.increasePoint(point);
	}

	@Transactional(readOnly = true)
	public List<User> findPkRankingUsers() {
		return userRepository.findAllByOrderByPointDesc(PageRequest.of(0, 100));
	}

	@Transactional
	public void resetUsersPoint() {

		List<User> usersWithPoints = userRepository.findByPointGreaterThan(0);

		if (!usersWithPoints.isEmpty()) {

			List<PkLog> resetLogs = usersWithPoints.stream()
				.map(user -> PkLog.builder()
					.pkType(PkType.RESET)
					.point(0)
					.user(user)
					.createdAt(LocalDateTime.now())
					.build())
				.toList();

			pkLogJdbcRepository.batchInsert(resetLogs);
		}

		userJdbcRepository.resetAllUserPoints();
	}
}
