package com.example.taste.domain.chat.entity;

import jakarta.persistence.Column;
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

import com.example.taste.common.entity.BaseCreatedAtEntity;
import com.example.taste.domain.chat.dto.ChatCreateRequestDto;
import com.example.taste.domain.party.entity.Party;
import com.example.taste.domain.user.entity.User;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "chat")
public class Chat extends BaseCreatedAtEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "party_id", nullable = false)
	private Party party;

	@Column(nullable = false)
	private String message;

	@Builder
	public Chat(ChatCreateRequestDto dto, User user, Party party) {
		this.user = user;
		this.party = party;
		this.message = dto.getMessage();
	}

	public void setUser(User user) {
		this.user = user;
	}
}
