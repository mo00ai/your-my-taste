package com.example.taste.domain.comment.entity;

import java.time.LocalDateTime;

import com.example.taste.common.entity.BaseEntity;
import com.example.taste.domain.board.entity.Board;
import com.example.taste.domain.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Comment extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String content;

	private LocalDateTime deletedAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "board_id", nullable = false)
	private Board board;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_comment")
	private Comment parent;

	//@OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
	//private List<Comment> children = new ArrayList<>();
	// children 필드는 지금 당장 필요 없어보임

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	public void setBoard(Board board) {
		this.board = board;
		if (!board.getCommentList().contains(this)) {
			board.getCommentList().add(this);
		}
	}

	@Builder
	public Comment(String content, LocalDateTime deletedAt, Board board, User user, Comment parent) {
		this.content = content;
		this.deletedAt = deletedAt;
		this.parent = parent;
		this.user = user;
		setBoard(board);
	}

	/*
	public void addChild(Comment child) {
		this.getChildren().add(child);
	}
	
	 */

	public void updateContent(String content) {
		this.content = content;
	}

	public void deleteContent(LocalDateTime deleteTime) {
		this.deletedAt = deleteTime;
	}
}
