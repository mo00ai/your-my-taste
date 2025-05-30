package com.example.taste.domain.event.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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
@Table(name = "event")
public class Event {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;

	private String contents;

	private LocalDate startDate;

	private LocalDate endDate;

	private boolean isActive;

	@OneToMany(mappedBy = "event", cascade = CascadeType.PERSIST)
	private List<BoardEvent> boardEventList = new ArrayList<>();

	@Builder
	public Event(String name, String contents, LocalDate startDate, LocalDate endDate, boolean isActive) {
		this.name = name;
		this.contents = contents;
		this.startDate = startDate;
		this.endDate = endDate;
		this.isActive = isActive;
	}

}
