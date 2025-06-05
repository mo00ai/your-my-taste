package com.example.taste.domain.pk.entity;

import java.time.LocalDateTime;

import com.example.taste.domain.pk.enums.PkType;
import com.example.taste.domain.user.entity.User;

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
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "pk_log")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PkLog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PkType pkType;

	@Column(nullable = false)
	private int point = 0;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Builder
	public PkLog(PkType pkType, Integer point, User user, LocalDateTime createdAt) {
		this.pkType = pkType;
		this.point = point != null ? point : 0;
		this.user = user;
		this.createdAt = createdAt;
	}

}
