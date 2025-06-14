package com.example.taste.domain.board.service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
		List<Hashtag> existingHashtag = findExistingHashtags(normalizedHashtags);
		// 새로운 해시태그 생성 및 저장
		List<Hashtag> newHashtags = createAndSaveNewHashtags(normalizedHashtags, existingHashtag);
		// 게시글에 해시태그 연결
		linkHashtagsToBoard(board, existingHashtag, newHashtags);

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
	 */
	private List<Hashtag> findExistingHashtags(List<String> hashtagNames) {
		return hashtagRepository.findByNameIn(hashtagNames);

	}

	private List<Hashtag> createAndSaveNewHashtags(List<String> normalizedHashtags, List<Hashtag> existingHashtags) {
		Set<String> existingNames = existingHashtags.stream()
			.map(Hashtag::getName)
			.collect(Collectors.toSet());

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
	private void linkHashtagsToBoard(Board board, List<Hashtag> existingHashtags, List<Hashtag> newHashtags) {

		List<BoardHashtag> boardHashtags = Stream.of(existingHashtags, newHashtags)
			.flatMap(List::stream)
			.map(hashtag -> BoardHashtag.builder()
				.board(board)
				.hashtag(hashtag)
				.build())
			.toList();
		// 기존 해시태그(DB)와 새로 추가된 해시태그 저장
		if (!boardHashtags.isEmpty()) {
			boardHashtagRepository.saveAll(boardHashtags);
		}
	}

	// 추후 valid 검증 추가 고려
	private boolean isValidHashtag(String hashtag) {
		return !hashtag.isEmpty() &&
			hashtag.length() <= 50;
	}

	/**
	 * 게시글에 해시태그 전부 삭제
	 */
	@Transactional
	public void clearBoardHashtags(Board board) {
		boardHashtagRepository.deleteAllByBoardId(board.getId());

	}

	/**
	 * 게시글에서 특정 해시태그 하나를 삭제합니다.
	 */
	@Transactional
	public void removeHashtagFromBoard(Board board, String hashtagName) {
		if (!StringUtils.hasText(hashtagName)) {
			return;
		}
		String normalizedName = hashtagName.trim().toLowerCase();

		boardHashtagRepository.findByBoardIdAndHashtag_Name(board.getId(), hashtagName)
			.ifPresent(boardHashtagRepository::delete);
	}

	/**
	 * 게시글에서 특정 해시태그 ID로 삭제합니다.
	 */
	@Transactional
	public void removeHashtagFromBoard(Board board, Long hashtagId) {
		if (hashtagId == null) {
			return;
		}
		boardHashtagRepository.findByBoardIdAndHashtagId(board.getId(), hashtagId)
			.ifPresent(boardHashtagRepository::delete);
	}

}
