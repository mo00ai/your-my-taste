package com.example.taste.domain.board.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
	name = "fcfs_information",
	uniqueConstraints = @UniqueConstraint(name = "uk_fcfs_board_user", columnNames = {"board_id", "user_id"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class FcfsInformation {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "board_id", nullable = false)
	private Long boardId;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Builder
	public FcfsInformation(Long boardId, Long userId) {
		this.boardId = boardId;
		this.userId = userId;
	}
}
