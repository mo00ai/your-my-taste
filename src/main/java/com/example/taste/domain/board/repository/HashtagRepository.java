package com.example.taste.domain.board.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.taste.domain.board.entity.Hashtag;

public interface HashtagRepository extends JpaRepository<Hashtag, Long> {

	List<Hashtag> findByNameIn(List<String> hashtagNames);
	
	boolean existsByName(String name);

}
