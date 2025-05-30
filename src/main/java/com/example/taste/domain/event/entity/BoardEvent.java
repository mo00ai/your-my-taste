package com.example.taste.domain.event.entity;

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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
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
}
