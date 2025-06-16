package com.example.taste.domain.board.entity;

import java.util.Objects;

import com.example.taste.common.exception.CustomException;
import com.example.taste.domain.board.exception.BoardErrorCode;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "board_hashtag")
public class BoardHashtag {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "board_id", nullable = false)
	private Board board;

	// orphanRemoval = true를 위한 equals()와 hashCode()메서드 오버라이딩

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "hashtag_id", nullable = false)
	private Hashtag hashtag;

	public void setBoard(Board board) {
		this.board = board;
		board.getBoardHashtagSet().add(this);

	}

	// 해시 태그 추가
	@Builder
	public BoardHashtag(Board board, Hashtag hashtag) {
		this.hashtag = hashtag;
		setBoard(board);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || !(o instanceof BoardHashtag)) {
			return false;
		}
		BoardHashtag that = (BoardHashtag)o;

		if (this.getBoard() == null || that.getBoard() == null) {
			throw new CustomException(BoardErrorCode.INVALID_BOARD_HASHTAG_STATE);

		}
		if (this.getHashtag() == null || that.getHashtag() == null) {
			throw new CustomException(BoardErrorCode.INVALID_BOARD_HASHTAG_STATE);

		}

		return Objects.equals(this.getBoard().getId(), that.getBoard().getId())
			&& Objects.equals(this.getHashtag().getName(), that.getHashtag().getName());
	}

	@Override
	public int hashCode() {
		if (this.getBoard() == null || this.getHashtag() == null) {
			throw new CustomException(BoardErrorCode.INVALID_BOARD_HASHTAG_STATE);
		}
		return Objects.hash(this.getBoard().getId(), this.getHashtag().getName());
	}
}
