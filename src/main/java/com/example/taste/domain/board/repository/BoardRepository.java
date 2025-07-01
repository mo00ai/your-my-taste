package com.example.taste.domain.board.repository;

import java.util.Collection;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.taste.domain.board.entity.AccessPolicy;
import com.example.taste.domain.board.entity.Board;
import com.example.taste.domain.board.entity.BoardType;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long>, BoardRepositoryCustom, BoardRepositoryJooqCustom {

	@Query("SELECT b FROM Board b WHERE b.id = :id and b.deletedAt is null ")
	Optional<Board> findActiveBoard(@Param("id") Long boardId);

	// 리팩토링 전 코드 - 성능 비교때 사용할 예정
	// @Query(value = """
	// 	    SELECT id
	// 		FROM board
	// 	    WHERE status = :status
	// 	    AND DATE_ADD(open_time, INTERVAL open_limit MINUTE) <= NOW()
	// 	""", nativeQuery = true)
	// List<Long> findExpiredTimeAttackBoardIds(@Param("status") String status);
	// @Modifying(clearAutomatically = true)
	// @Query(value = """
	// 	    UPDATE board
	// 	    SET status = 'CLOSED'
	// 	    WHERE id IN (:ids)
	// 	""", nativeQuery = true)
	// long closeBoardsByIds(@Param("ids") List<Long> ids);

	// @EntityGraph(attributePaths = {"user", "user.image"})
	// Page<Board> findByTypeEqualsAndAccessPolicyInAndDeletedAtIsNull(BoardType type, Collection<AccessPolicy> statuses,
	// 	Pageable pageable);
	//
	// default Page<Board> findUndeletedBoardByTypeAndPolicy(BoardType type, Collection<AccessPolicy> statuses,
	// 	Pageable pageable) {
	// 	return findByTypeEqualsAndAccessPolicyInAndDeletedAtIsNull(type, statuses, pageable);
	// }
	//  pg_trgm을 이용한 텍스트 유사도 검색
	// @Query(value = """
	// 	SELECT * FROM posts
	// 	WHERE similarity(title, :keyword) > 0.3
	// 	   OR similarity(content, :keyword) > 0.3
	// 	ORDER BY GREATEST(
	// 	    similarity(title, :keyword),
	// 	    similarity(content, :keyword)
	// 	) DESC
	// 	""", nativeQuery = true)
	// List<Board> findByTextSimilarity(@Param("keyword") String keyword);

	Page<Board> findByTypeEqualsAndAccessPolicyInAndDeletedAtIsNull(BoardType type, Collection<AccessPolicy> statuses,
		Pageable pageable);
}
