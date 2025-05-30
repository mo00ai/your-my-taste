package com.example.taste.domain.board.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.taste.domain.event.entity.BoardEvent;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "board")
public class Board {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String title;

	@Column(nullable = false)
	private String contents;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private BoardType type = BoardType.N;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private BoardStatus status = BoardStatus.OPEN;

	private int openLimit; // 단위 : 분(TIMEATTACK), 인원 수(FCFS)

	private LocalDateTime openTime;

	// TODO deletedAt BaseEntity상속 예정

	// TODO 사용자 연관관계 설정 필요

	// 게시글 해시태그 연관관계
	@OneToMany(mappedBy = "board", cascade = CascadeType.PERSIST)
	private List<BoardHashtag> boardHashtagList = new ArrayList<>();

	// 이벤트 신청 게시글 연관관계
	@OneToMany(mappedBy = "board", cascade = CascadeType.PERSIST)
	private List<BoardEvent> boardEventList = new ArrayList<>();

	// 공감 연관관계
	@OneToMany(mappedBy = "board", cascade = CascadeType.PERSIST)
	private List<Like> likeList = new ArrayList<>();

	// TODO 연관관계 메서드 필요
	@Builder
	public Board(String title, String contents, BoardType type, BoardStatus status) {
		this.title = title;
		this.contents = contents;
		this.type = type != null ? type : BoardType.N;
		this.status = status != null ? status : BoardStatus.OPEN;
	}

	// 오버로딩된 빌더 생성자
	@Builder(builderMethodName = "hBoardBuilder")
	public Board(String title, String contents, BoardType type, BoardStatus status, Integer openLimit,
		LocalDateTime openTime) {
		this.title = title;
		this.contents = contents;
		this.type = type != null ? type : BoardType.H;
		this.status = status != null ? status : BoardStatus.CLOSED;  // 홍대병 전용이지만 혹시 파라미터를 안 넣으면 게시글 보이지 않도록
		this.openLimit = openLimit;
		this.openTime = openTime;
	}

}
