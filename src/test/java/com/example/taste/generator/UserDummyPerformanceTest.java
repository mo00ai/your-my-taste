package com.example.taste.generator;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import com.example.taste.domain.party.enums.InvitationStatus;
import com.example.taste.domain.party.enums.InvitationType;
import com.example.taste.domain.party.enums.MatchStatus;
import com.example.taste.domain.store.repository.StoreRepository;
import com.example.taste.domain.user.enums.Gender;
import com.example.taste.domain.user.enums.Level;
import com.example.taste.domain.user.enums.Role;
import com.example.taste.domain.user.repository.UserRepository;

@Tag("Performance")
@ActiveProfiles("local")    // 로컬 테스트
@SpringBootTest
public class UserDummyPerformanceTest {
	@Autowired
	JdbcTemplate jdbcTemplate;

	@Autowired
	UserRepository userRepository;

	@Autowired
	StoreRepository storeRepository;

	static int BATCH_SIZE = 100;
	static int TOTAL = BATCH_SIZE * 5;
	static int DAYS_AFTER_NOW = 2;

	@Test
	@DisplayName("유저 1000개 더미 데이터 생성")
	void dummyUserBulkInsert() {
		String INSERT_SQL =
			"INSERT INTO users (created_at, email, password, address, age,"
				+ "follower, following, level, nickname, posting_count, role, point, gender) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		// List<Gender> genderOptions = new ArrayList<>(List.of(Gender.values()));
		// genderOptions.remove(Gender.ANY);    // FEMALE, MALE 만

		// 더미 유저 생성
		for (int batch = 0; batch < TOTAL * 2 / BATCH_SIZE; batch++) {
			int finalBatch = batch;
			jdbcTemplate.batchUpdate(INSERT_SQL, new BatchPreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement ps, int i) throws SQLException {
					int index = finalBatch * BATCH_SIZE + i;

					ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
					ps.setString(2, "test" + (index + 1) + "@email.com");  //
					ps.setString(3, "$2a$10$qPYsAOs.Ai3woO0w5IuFcO8Hn.hsK.UeANOTPCJM0cR.Hg45kNTKm");    // 비번: asdf@1234
					ps.setString(4, "address");
					ps.setInt(5, 30);
					ps.setInt(6, 0);
					ps.setInt(7, 0);
					ps.setString(8, Level.NORMAL.toString());
					ps.setString(9, "nickname" + (index + 1));
					ps.setInt(10, 0);
					ps.setString(11, Role.USER.toString());
					ps.setInt(12, 0);
					ps.setString(13, Gender.FEMALE.toString());
				}

				@Override
				public int getBatchSize() {
					return BATCH_SIZE;
				}
			});
		}
	}

	@Test
	@DisplayName("유저 매칭 인포 500개 더미 데이터 생성")
	void dummyUserMatchInfoBulkInsert() {
		String INSERT_SQL =
			"INSERT INTO user_match_info (created_at, match_status,"
				+ "meeting_date, title, user_age, user_gender, user_id) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?)";

		Gender[] genderOptions = Gender.values();

		for (int batch = 0; batch < TOTAL / BATCH_SIZE; batch++) {
			int finalBatch = batch;
			jdbcTemplate.batchUpdate(INSERT_SQL, new BatchPreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement ps, int i) throws SQLException {
					int index = finalBatch * BATCH_SIZE + i;
					// int minAge = ThreadLocalRandom.current().nextInt(20, 50) / 10 * 10; // 10단위 나잇대 (20, 30, 40 중 하나)
					// int maxAge = ThreadLocalRandom.current().nextInt(minAge, 51); // minAge 이상, 최대 50까지

					ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now())); // created_at
					ps.setString(2, MatchStatus.IDLE.toString());                                    // match_status
					ps.setDate(3, Date.valueOf(LocalDate.now().plusDays(DAYS_AFTER_NOW))); // meeting_date
					// ps.setString(6, "Seoul");                                   // region
					ps.setString(4, "UserMatchInfo" + (index + 1));         // title
					ps.setInt(5, 30);  // user_age
					ps.setString(6, Gender.FEMALE.toString());                                    // user_gender
					ps.setLong(7, index + 1);               // user_id
				}

				@Override
				public int getBatchSize() {
					return BATCH_SIZE;
				}
			});
		}
	}

	@Test
	@DisplayName("유저 매칭 인포 가게 500개 더미 데이터 생성")
	void dummyUserMatchInfoStoreBulkInsert() {
		String INSERT_SQL =
			"INSERT INTO user_match_info_store (store_id, user_match_info_id) "
				+ "VALUES (?, ?)";

		Gender[] genderOptions = Gender.values();

		for (int batch = 0; batch < TOTAL / BATCH_SIZE; batch++) {
			int finalBatch = batch;
			jdbcTemplate.batchUpdate(INSERT_SQL, new BatchPreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement ps, int i) throws SQLException {
					int index = finalBatch * BATCH_SIZE + i;
					ps.setLong(1, 1L);
					ps.setLong(2, (index + 1));
				}

				@Override
				public int getBatchSize() {
					return BATCH_SIZE;
				}
			});
		}
	}

	@Test
	@DisplayName("파티 더미 데이터 500개 더미 데이터 생성")
	void insertPartyDummyData() {
		// 파티장 1, 가게 1, 파티 5000
		// 스토어 1개 생성
		storeRepository.deleteById(1L);
		String INSERT_STORE_SQL =
			"INSERT INTO store (id, created_at, updated_at, category_id, name, description, address, road_address, mapx, mapy) "
				+
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		jdbcTemplate.update(INSERT_STORE_SQL,
			1L,
			Timestamp.valueOf(LocalDateTime.now()),
			Timestamp.valueOf(LocalDateTime.now()),
			4L, // category_id, 양식
			"Pizza Store",
			"테스트 가게 설명",
			"테스트 주소",
			"테스트 도로명 주소",
			BigDecimal.valueOf(127.1234567),
			BigDecimal.valueOf(37.1234567)
		);

		// 유저 1명 생성 (파티장 id=99999)
		userRepository.deleteById(99999L);
		String INSERT_USER_SQL =
			"INSERT INTO users (id, created_at, email, password, address, age, follower,"
				+ "following, level, nickname, posting_count, role, point, gender) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		jdbcTemplate.update(INSERT_USER_SQL,
			99999L,
			Timestamp.valueOf(LocalDateTime.now()),
			"host@email.com",
			"$2a$10$qPYsAOs.Ai3woO0w5IuFcO8Hn.hsK.UeANOTPCJM0cR.Hg45kNTKm", // 비번: asdf@1234
			"주소",
			30,
			0,
			0,
			"NORMAL",
			"파티호스트닉네임",
			0,
			"USER",
			0,
			"MALE"
		);

		// 4. 파티 500개 생성
		String INSERT_PARTY_SQL =
			"INSERT INTO party (created_at, updated_at, user_id, title, description,"
				+ "party_status, store_id, meeting_date, max_members, now_members, enable_random_matching) "
				+
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		for (int batch = 0; batch < TOTAL / BATCH_SIZE; batch++) {
			int finalBatch = batch;
			jdbcTemplate.batchUpdate(INSERT_PARTY_SQL, new BatchPreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement ps, int i) throws SQLException {
					int index = finalBatch * BATCH_SIZE + i;

					ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now())); // created_at
					ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now())); // updated_at
					ps.setLong(3, 99999L); // host_user_id
					ps.setString(4, "TestParty" + (index + 1)); // title
					ps.setString(5, "테스트 파티 설명");
					ps.setString(6, "ACTIVE"); // party_status
					ps.setLong(7, 1L); // store_id
					ps.setDate(8, Date.valueOf(LocalDate.now().plusDays(DAYS_AFTER_NOW))); // meeting_date
					ps.setInt(9, 4); // max_members
					ps.setInt(10, 1); // now_members
					ps.setBoolean(11, true); // enable_random_matching
				}

				@Override
				public int getBatchSize() {
					return BATCH_SIZE;
				}
			});
		}
	}

	@Test
	@DisplayName("파티 초대(파티장) 더미 데이터 500개 더미 데이터 생성")
	void insertPartyInvitationDummyData() {
		// 파티 초대 500개 생성 (파티장 가입용)
		String INSERT_PARTY_INVITATION_SQL =
			"INSERT INTO party_invitation (party_id, user_id, user_match_info_id, "
				+ "invitation_type, invitation_status) "
				+ "VALUES (?, ?, ?, ?, ?)";

		for (int batch = 0; batch < TOTAL / BATCH_SIZE; batch++) {
			int finalBatch = batch;

			jdbcTemplate.batchUpdate(INSERT_PARTY_INVITATION_SQL, new BatchPreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement ps, int i) throws SQLException {
					int index = finalBatch * BATCH_SIZE + i;

					ps.setLong(1, (index + 1));
					ps.setLong(2, 99999L);
					ps.setNull(3, Types.BIGINT);
					ps.setString(4, InvitationType.INVITATION.toString());
					ps.setString(5, InvitationStatus.CONFIRMED.toString());
				}

				@Override
				public int getBatchSize() {
					return BATCH_SIZE;
				}
			});
		}
	}

	@Test
	@DisplayName("파티 랜덤매칭 정보 500개 생성")
	void dummyPartyMatchInfoInsert() {

		String INSERT_SQL =
			"INSERT INTO party_match_info (party_id, store_id, meeting_date, gender, match_status) "
				+ "VALUES (?, ?, ?, ?, ?)";

		for (int batch = 0; batch < TOTAL / BATCH_SIZE; batch++) {
			int finalBatch = batch;

			jdbcTemplate.batchUpdate(INSERT_SQL, new BatchPreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement ps, int i) throws SQLException {
					int index = finalBatch * BATCH_SIZE + i;

					ps.setLong(1, index + 1); // party_id (1부터 5000까지)
					ps.setLong(2, 1L); // store_id
					ps.setDate(3, Date.valueOf(LocalDate.now().plusDays(2))); // meeting_date
					// ps.setInt(4, 20); // min_age
					// ps.setInt(5, 50); // max_age
					ps.setString(4, Gender.ANY.name());
					ps.setString(5, MatchStatus.MATCHING.toString()); // match_status
				}

				@Override
				public int getBatchSize() {
					return BATCH_SIZE;
				}
			});
		}
	}
}
