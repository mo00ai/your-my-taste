package com.example.taste.domain.board.entity;

import static com.example.taste.domain.board.exception.BoardErrorCode.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.taste.common.entity.SoftDeletableEntity;
import com.example.taste.common.exception.CustomException;
import com.example.taste.domain.board.dto.request.BoardUpdateRequestDto;
import com.example.taste.domain.comment.entity.Comment;
import com.example.taste.domain.event.entity.BoardEvent;
import com.example.taste.domain.image.entity.BoardImage;
import com.example.taste.domain.store.entity.Store;
import com.example.taste.domain.user.entity.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
@Table(name = "board")
public class Board extends SoftDeletableEntity {
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

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "store_id", nullable = false)
	private Store store;

	// 게시글 해시태그 연관관계
	@OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<BoardHashtag> boardHashtagList = new ArrayList<>();

	// 이벤트 신청 게시글 연관관계
	@OneToMany(mappedBy = "board", cascade = CascadeType.PERSIST)
	private List<BoardEvent> boardEventList = new ArrayList<>();

	// 공감 연관관계
	@OneToMany(mappedBy = "board", cascade = CascadeType.PERSIST)
	private List<Like> likeList = new ArrayList<>();

	// 댓글 연관관계
	@OneToMany(mappedBy = "board", cascade = CascadeType.PERSIST)
	private List<Comment> commentList = new ArrayList<>();

	// 게시글 이미지 연관관계
	@OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<BoardImage> boardImageList = new ArrayList<>();

	// 이벤트와 게시글 양방향 등록
	public void register(Store store, User user) {
		this.store = store;
		this.user = user;

		if (!user.getBoardList().contains(this)) {
			user.getBoardList().add(this);
		}

	}

	@Builder
	public Board(String title, String contents, BoardType type, BoardStatus status, Store store, User user) {
		this.title = title;
		this.contents = contents;
		this.type = type != null ? type : BoardType.N;
		this.status = status != null ? status : BoardStatus.OPEN;
		register(store, user);
	}

	// 오버로딩된 빌더 생성자
	@Builder(builderMethodName = "oBoardBuilder")
	public Board(String title, String contents, BoardType type, BoardStatus status, Integer openLimit,
		LocalDateTime openTime, Store store, User user) {
		this.title = title;
		this.contents = contents;
		this.type = type != null ? type : BoardType.O;
		this.status = status != null ? status : BoardStatus.CLOSED;  // 오픈런 전용이지만 혹시 파라미터를 안 넣으면 게시글 보이지 않도록
		this.openLimit = openLimit;
		this.openTime = openTime;
		register(store, user);
	}

	// 해시태그 삭제
	public void removeBoardHashtag(BoardHashtag boardHashtag) {
		boardHashtagList.remove(boardHashtag);
	}

	public void update(BoardUpdateRequestDto requestDto) {
		if (requestDto.getTitle() != null) {
			this.title = requestDto.getTitle();
		}
		if (requestDto.getContents() != null) {
			this.contents = requestDto.getContents();
		}
		if (requestDto.getType() != null) {
			this.type = BoardType.from(requestDto.getType());
		}
	}

	public void updateStatusClosed() {
		this.status = BoardStatus.CLOSED;
	}

	// 게시글의 공개 종료시각 <= 현재시각이면 error
	public void validateAndCloseIfExpired() {
		if (!this.openTime.plusMinutes(this.openLimit)
			.isAfter(LocalDateTime.now())) {
			updateStatusClosed();
			throw new CustomException(CLOSED_BOARD);
		}
	}
}
