package com.example.taste.domain.board.entity;

import com.example.taste.common.exception.CustomException;
import com.example.taste.domain.board.exception.BoardErrorCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "hashtag")
public class Hashtag {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 50, unique = true)
	private String name;

	@Builder
	public Hashtag(String name) {
		if (name == null || name.trim().isEmpty()) {
			throw new CustomException(BoardErrorCode.INVALID_HASHTAG);
		}
		this.name = name.trim().toLowerCase();
	}
}
