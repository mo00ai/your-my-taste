package com.example.taste.domain.board.entity;

import java.util.Objects;

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
	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass())
			return false;
		BoardHashtag that = (BoardHashtag)o;
		return Objects.equals(id, that.id) && Objects.equals(board, that.board)
			&& Objects.equals(hashtag, that.hashtag);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, board, hashtag);
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "hashtag_id", nullable = false)
	private Hashtag hashtag;

	public void setBoard(Board board) {
		this.board = board;
		if (!board.getBoardHashtagList().contains(this)) {
			board.getBoardHashtagList().add(this);
		}
	}

	// 해시 태그 추가
	@Builder
	public BoardHashtag(Board board, Hashtag hashtag) {
		this.hashtag = hashtag;
		setBoard(board);
	}

}
