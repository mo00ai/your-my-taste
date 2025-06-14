package com.example.taste.domain.board.entity;

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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(of = "id", callSuper = false) // id값으로만 비교
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

}
