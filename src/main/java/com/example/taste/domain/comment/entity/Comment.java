package com.example.taste.domain.comment.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
import jakarta.persistence.OneToMany;
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
	private String contents;

	private LocalDateTime deletedAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "board_id", nullable = false)
	private Board board;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_comment")
	private Comment parent;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "root_comment")
	private Comment root;

	@OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
	private List<Comment> children = new ArrayList<>();

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	public void setBoard(Board board) {
		this.board = board;
		if (!board.getCommentList().contains(this)) {
			board.getCommentList().add(this);
		}
	}

	public void setParent(Comment parent) {
		this.parent = parent;
		if (parent != null) {
			parent.addChild(this);
		}
	}

	@Builder
	public Comment(String contents, LocalDateTime deletedAt, Board board, User user, Comment parent, Comment root) {
		this.contents = contents;
		this.deletedAt = deletedAt;
		this.user = user;
		this.root = root;
		setBoard(board);
		setParent(parent);
	}

	public void addChild(Comment child) {
		this.getChildren().add(child);
	}

	public void updateContents(String contents) {
		this.contents = contents;
	}

	public void deleteContent(LocalDateTime deleteTime) {
		this.deletedAt = deleteTime;
	}
}
