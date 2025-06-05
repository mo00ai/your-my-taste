package com.example.taste.domain.event.entity;

import java.util.Objects;

import com.example.taste.common.entity.BaseEntity;
import com.example.taste.domain.board.entity.Board;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "board_event")
public class BoardEvent extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "board_id", nullable = false)
	private Board board;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "event_id", nullable = false)
	private Event event;

	// 이벤트와 게시글 양방향 등록
	public void register(Event event, Board board) {
		this.event = event;
		this.board = board;

		if (!board.getBoardEventList().contains(this)) {
			board.getBoardEventList().add(this);
		}

		if (!event.getBoardEventList().contains(this)) {
			event.getBoardEventList().add(this);
		}

	}

	@Builder
	public BoardEvent(Event event, Board board) {
		this.register(event, board);
	}

	// 삭제시 연관관계 해제
	public void unregister() {
		if (this.board != null) {
			this.board.getBoardEventList().remove(this);
		}
		if (this.event != null) {
			this.event.getBoardEventList().remove(this);
		}
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass())
			return false;
		BoardEvent that = (BoardEvent)o;
		return Objects.equals(id, that.id) && Objects.equals(board, that.board)
			&& Objects.equals(event, that.event);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, board, event);
	}
}
