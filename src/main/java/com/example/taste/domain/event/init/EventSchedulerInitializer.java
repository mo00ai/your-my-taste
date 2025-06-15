package com.example.taste.domain.event.init;

import java.time.LocalDate;
import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.example.taste.domain.board.entity.Board;
import com.example.taste.domain.board.entity.Like;
import com.example.taste.domain.board.repository.BoardRepository;
import com.example.taste.domain.board.repository.LikeRepository;
import com.example.taste.domain.event.entity.BoardEvent;
import com.example.taste.domain.event.entity.Event;
import com.example.taste.domain.event.repository.BoardEventRepository;
import com.example.taste.domain.event.repository.EventRepository;
import com.example.taste.domain.store.entity.Store;
import com.example.taste.domain.store.repository.CategoryRepository;
import com.example.taste.domain.store.repository.StoreRepository;
import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventSchedulerInitializer implements ApplicationRunner {

	private final UserRepository userRepository;
	private final StoreRepository storeRepository;
	private final BoardRepository boardRepository;
	private final EventRepository eventRepository;
	private final BoardEventRepository boardEventRepository;
	private final LikeRepository likeRepository;
	private final CategoryRepository categoryRepository;

	@Transactional
	@Override
	public void run(ApplicationArguments args) {
		log.info("[더미 데이터 생성] 이벤트 테스트 데이터 생성 시작");

		// 유저 3명 (1~3)
		User user1 = userRepository.findById(4L).orElseThrow(); //이벤트 당첨자가 되어야 함
		User user2 = userRepository.findById(5L).orElseThrow();
		User user3 = userRepository.findById(6L).orElseThrow();

		userRepository.saveAll(List.of(user1, user2, user3));

		//카테고리, 가게는 이미 생성됨

		// 임의 Store 1개
		Store store = storeRepository.findAll().stream().findFirst().orElseThrow();

		// 이벤트 생성 (어제 시작 ~ 어제 끝)
		Event event = Event.builder()
			.name("김치찌개 왕 선발대회")
			.contents("김치찌개 짱 맛집 추천해줘!")
			.startDate(LocalDate.now().minusDays(1))
			.endDate(LocalDate.now().minusDays(1))
			.isActive(false)
			.user(user1)
			.build();
		eventRepository.save(event);

		// 게시글 3개 생성
		Board board1 = Board.builder()
			.title("1번의 김치찌개 맛집")
			.contents("진짜 맛있어요!")
			.type(null)
			.status(null)
			.user(user1)
			.store(store)
			.build();
		boardRepository.save(board1);

		Board board2 = Board.builder()
			.title("2번의 김치찌개")
			.contents("우리 동네 최강!")
			.user(user2)
			.store(store)
			.build();
		boardRepository.save(board2);

		Board board3 = Board.builder()
			.title("3번 찌개도 괜찮음")
			.contents("무난한 맛집")
			.user(user3)
			.store(store)
			.build();
		boardRepository.save(board3);

		// 이벤트에 신청 (BoardEvent)
		boardEventRepository.saveAll(List.of(
			BoardEvent.builder().event(event).board(board1).build(),
			BoardEvent.builder().event(event).board(board2).build(),
			BoardEvent.builder().event(event).board(board3).build()
		));

		// 좋아요: board1 = 2명(user2, user3), board2 = 1명(user1)
		likeRepository.saveAll(List.of(
			Like.builder().user(user2).board(board1).build(),
			Like.builder().user(user3).board(board1).build(),
			Like.builder().user(user1).board(board2).build()
		));

		log.info("[더미 데이터 생성] 이벤트 테스트 데이터 생성 완료");
	}
}
