package com.example.taste.domain.event.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.example.taste.domain.user.entity.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "event")
public class Event {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private String contents;

	@Column(nullable = false)
	private LocalDate startDate;

	@Column(nullable = false)
	private LocalDate endDate;

	@Column(nullable = false)
	private boolean isActive;

	@OneToMany(mappedBy = "event", cascade = CascadeType.PERSIST)
	private List<BoardEvent> boardEventList = new ArrayList<>();

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	public void setUser(User user) {
		this.user = user;
		if (!user.getEventList().contains(this)) {
			user.getEventList().add(this);
		}
	}

	@Builder
	public Event(String name, String contents, LocalDate startDate, LocalDate endDate, boolean isActive, User user) {

		this.name = name;
		this.contents = contents;
		this.startDate = startDate;
		this.endDate = endDate;
		this.isActive = isActive;
		setUser(user);
	}

}
