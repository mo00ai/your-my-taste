package com.example.taste.domain.board.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.example.taste.common.entity.SoftDeletableEntity;
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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@EqualsAndHashCode(of = "id", callSuper = false) // id값으로만 비교
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
	private AccessPolicy accessPolicy = AccessPolicy.OPEN;

	private Integer openLimit; // 단위 : 분(TIMEATTACK), 인원 수(FCFS)

	private LocalDateTime openTime;
	@Setter
	// 검색용 인덱스 추가
	@Column(name = "search_keywords", columnDefinition = "TEXT")
	private String searchKeywords;  // 전체 키워드 (공백으로 구분)

	@Setter
	@Column(name = "search_nouns", columnDefinition = "TEXT")
	private String searchNouns;     // 명사만 (공백으로 구분)

	@Setter
	@Column(name = "search_phrases", columnDefinition = "TEXT")
	private String searchPhrases;   // 구문만 (공백으로 구분)

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "store_id", nullable = false)
	private Store store;

	// 게시글 해시태그 연관관계
	@OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<BoardHashtag> boardHashtagSet = new HashSet<>();

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
	}

	@Builder(builderMethodName = "nBoardBuilder", buildMethodName = "buildNormal")
	public Board(String title, String contents, String type, String accessPolicy, Store store, User user) {
		this.title = title;
		this.contents = contents;
		this.type = type != null ? BoardType.from(type) : BoardType.N;
		this.accessPolicy =
			accessPolicy != null ? AccessPolicy.from(accessPolicy) : AccessPolicy.OPEN;
		register(store, user);
	}

	// 오버로딩된 빌더 생성자
	@Builder(builderMethodName = "oBoardBuilder", buildMethodName = "buildOpenRun")
	public Board(String title, String contents, String type, String accessPolicy,
		Integer openLimit, LocalDateTime openTime, Store store, User user) {
		this.title = title;
		this.contents = contents;
		this.type = type != null ? BoardType.from(type) : BoardType.O;
		this.accessPolicy = accessPolicy != null ? AccessPolicy.from(accessPolicy) :
			AccessPolicy.CLOSED;  // 오픈런 전용이지만 혹시 파라미터를 안 넣으면 게시글 보이지 않도록
		this.openLimit = openLimit;
		this.openTime = openTime;
		register(store, user);
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

	public boolean isNBoard() {
		return this.type == BoardType.N;
	}

	public boolean isOverOpenLimit(Long num) {
		return this.openLimit <= num;
	}
}
