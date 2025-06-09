package com.example.taste.domain.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.taste.domain.event.entity.Event;

public interface EventRepository extends JpaRepository<Event, Long>, EventRepositoryCustom {

}
