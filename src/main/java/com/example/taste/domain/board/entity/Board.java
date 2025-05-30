package com.example.taste.domain.board.entity;

import java.time.LocalDateTime;

import com.example.taste.common.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "board")
public class Board extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String title;

	@Column(nullable = false)
	private String contents;

	@Enumerated(EnumType.STRING)
	private BoardType type;

	@Enumerated(EnumType.STRING)
	private BoardStatus status = BoardStatus.OPEN;

	private int openLimit;

	private LocalDateTime openTime;

	private LocalDateTime deletedAt;

}
