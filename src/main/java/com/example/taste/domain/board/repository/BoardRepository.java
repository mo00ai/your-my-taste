package com.example.taste.domain.board.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.taste.domain.board.dto.response.BoardListResponseDto;
import com.example.taste.domain.board.entity.Board;

import io.lettuce.core.dynamic.annotation.Param;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long>, BoardRepositoryCustom {

	@Query("SELECT b FROM Board b WHERE b.id = :id and b.deletedAt is null ")
	Optional<Board> findActiveBoard(@Param("id") Long boardId);

	@Query("""
		select new com.example.taste.domain.board.dto.response.BoardListResponseDto(
		    b.id,
		    b.title,
		    s.name,
		    u.nickname,
		    i.url
		)
		from Board b
		join b.store s
		join b.user u
		left join b.boardImageList bi
		left join bi.image i
		where u.id in :userIds
		group by b.id, s.name, u.nickname, b.title, i.url
		order by b.createdAt desc
		""")
	List<BoardListResponseDto> findBoardListDtoByUserIdList(@Param("userIds") List<Long> userIds, Pageable pageable);

}
