package com.example.taste.domain.board.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.example.taste.domain.board.entity.Board;
import com.example.taste.domain.board.entity.BoardHashtag;
import com.example.taste.domain.board.entity.Hashtag;
import com.example.taste.domain.board.repository.BoardHashtagRepository;
import com.example.taste.domain.board.repository.HashtagRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HashtagService {

	private final HashtagRepository hashtagRepository;
	private final BoardHashtagRepository boardHashtagRepository;

	@Transactional
	public void applyHashtagsToBoard(Board board, List<String> hashtagList) {

		if (hashtagList == null || hashtagList.isEmpty()) {
			return;
		}

		List<String> normalizedHashtags = normalizeHashtags(hashtagList);
		if (normalizedHashtags.isEmpty()) {
			return;
		}
		// 기존 해시태그 조회
		Map<String, Hashtag> existingHashtagMap = findExistingHashtags(normalizedHashtags);
		// 새로운 해시태그 생성 및 저장
		List<Hashtag> newHashtags = createAndSaveNewHashtags(normalizedHashtags, existingHashtagMap.keySet());
		// 게시글에 해시태그 연결
		linkHashtagsToBoard(board, existingHashtagMap, newHashtags);

	}

	private List<String> normalizeHashtags(List<String> rawHashtags) {
		return rawHashtags.stream()
			.filter(Objects::nonNull)        // null제거
			.map(String::trim)
			.filter(StringUtils::hasText)    // 공백이 아닌 문자열이 존재 검증
			.map(String::toLowerCase)        // 소문자
			.filter(this::isValidHashtag)    // 해시태그 길이 유효성 검사
			.distinct()        // 중복 제거
			.collect(Collectors.toList());
	}

	/**
	 * 기존 해시태그를 조회합니다.(DB에 이미 존재하는 해시태그 조회)
	 * 키는 해시태그 이름, 벨류는 엔티티
	 */
	private Map<String, Hashtag> findExistingHashtags(List<String> hashtagNames) {
		return hashtagRepository.findByNameIn(hashtagNames).stream()
			.collect(Collectors.toMap(Hashtag::getName, Function.identity()));
	}

	private List<Hashtag> createAndSaveNewHashtags(List<String> normalizedHashtags, Set<String> existingNames) {
		List<Hashtag> newHashtags = normalizedHashtags.stream()
			.filter(name -> !existingNames.contains(name))
			.map(name -> Hashtag.builder()
				.name(name)
				.build())
			.collect(Collectors.toList());

		if (!newHashtags.isEmpty()) {
			return hashtagRepository.saveAll(newHashtags);
		}

		return newHashtags;
	}

	/**
	 * 게시글에 해시태그 연결
	 */
	private void linkHashtagsToBoard(Board board, Map<String, Hashtag> existingHashtagMap, List<Hashtag> newHashtags) {
		List<BoardHashtag> boardHashtags = existingHashtagMap.values().stream()
			.map(hashtag -> BoardHashtag.builder()
				.board(board)
				.hashtag(hashtag)
				.build())
			.collect(Collectors.toList());

		// 새로 생성된 해시태그들도 추가
		List<BoardHashtag> newBoardHashtags = newHashtags.stream()
			.map(hashtag -> BoardHashtag.builder()
				.board(board)
				.hashtag(hashtag)
				.build())
			.collect(Collectors.toList());

		boardHashtags.addAll(newBoardHashtags);
		// 기존 해시태그(DB)와 새로 추가된 해시태그 저장
		if (!boardHashtags.isEmpty()) {
			boardHashtagRepository.saveAll(boardHashtags);
		}
	}

	// 추후 valid 검증 추가 고려
	private boolean isValidHashtag(String hashtag) {
		return hashtag.length() >= 1 &&
			hashtag.length() <= 50;
	}

	/**
	 * 게시글에 해시태그 전부 삭제
	 */
	public void clearBoardHashtags(Board board) {
		// Board 엔티티의 removeBoardHashtag 메서드를 활용
		List<BoardHashtag> hashtagsToRemove = new ArrayList<>(board.getBoardHashtagList());
		// 엔티티를 통해 삭제
		hashtagsToRemove.forEach(board::removeBoardHashtag);

	}

	/**
	 * 게시글에서 특정 해시태그 하나를 삭제합니다.
	 */
	@Transactional
	public void removeHashtagFromBoard(Board board, String hashtagName) {
		if (hashtagName == null || hashtagName.trim().isEmpty()) {
			return;
		}

		String normalizedName = hashtagName.trim().toLowerCase();

		// 해당 해시태그를 가진 BoardHashtag 찾기
		BoardHashtag targetBoardHashtag = board.getBoardHashtagList().stream()
			.filter(bh -> bh.getHashtag().getName().equals(normalizedName))
			.findFirst()
			.orElse(null);

		if (targetBoardHashtag != null) {
			// Board 엔티티의 removeBoardHashtag 메서드 사용
			board.removeBoardHashtag(targetBoardHashtag);
		}
	}

	/**
	 * 게시글에서 특정 해시태그 ID로 삭제합니다.
	 */
	@Transactional
	public void removeHashtagFromBoard(Board board, Long hashtagId) {
		if (hashtagId == null) {
			return;
		}

		// 해당 해시태그 ID를 가진 BoardHashtag 찾기
		BoardHashtag targetBoardHashtag = board.getBoardHashtagList().stream()
			.filter(bh -> bh.getHashtag().getId().equals(hashtagId))
			.findFirst()
			.orElse(null);

		if (targetBoardHashtag != null) {
			// Board 엔티티의 removeBoardHashtag 메서드 사용
			board.removeBoardHashtag(targetBoardHashtag);
		}
	}
}
