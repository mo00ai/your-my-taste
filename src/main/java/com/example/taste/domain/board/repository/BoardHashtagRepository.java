package com.example.taste.domain.board.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.example.taste.domain.board.entity.BoardHashtag;

public interface BoardHashtagRepository extends JpaRepository<BoardHashtag, Long> {

	// 해시태그 이름으로 확인
	Optional<BoardHashtag> findByBoardIdAndHashtag_Name(Long boardId, String hashtagName);

	// 해시태그 id으로 확인
	Optional<BoardHashtag> findByBoardIdAndHashtagId(Long boardId, Long hashtagId);

	@Modifying
	@Query("delete from BoardHashtag bh where bh.board.id =:boardid	")
	void deleteAllByBoardId(Long boardId);
	
}
