package com.example.taste.domain.user.entity;

import static com.example.taste.domain.pk.exception.PkErrorCode.*;

import java.util.ArrayList;
import java.util.List;

import com.example.taste.common.entity.SoftDeletableEntity;
import com.example.taste.common.exception.CustomException;
import com.example.taste.domain.auth.dto.SignupRequestDto;
import com.example.taste.domain.board.entity.Board;
import com.example.taste.domain.event.entity.Event;
import com.example.taste.domain.image.entity.Image;
import com.example.taste.domain.user.dto.request.UserUpdateRequestDto;
import com.example.taste.domain.user.enums.Gender;
import com.example.taste.domain.user.enums.Level;
import com.example.taste.domain.user.enums.Role;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Entity
@NoArgsConstructor        // TODO: 추후 PROTECTED 로 리팩토링
@Table(name = "users")
public class User extends SoftDeletableEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Setter
	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "image_id")
	private Image image;

	@Column(nullable = false)
	private String nickname;

	@Column(nullable = false, unique = true)
	private String email;

	@Column(nullable = false)
	private String password;

	@Column(nullable = false)
	private String address;

	@Enumerated(EnumType.STRING)
	private Gender gender;

	private int age;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Role role;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Level level = Level.NORMAL;

	@Column(nullable = false)
	private int postingCount = 0;
	private int point = 0;

	@Column(nullable = false)
	private int follower = 0;

	@Column(nullable = false)
	private int following = 0;

	@OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
	private List<Board> boardList = new ArrayList<>();

	@OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
	private List<Event> eventList = new ArrayList<>();

	@OneToMany(mappedBy = "user", fetch = FetchType.LAZY,
		cascade = CascadeType.ALL, orphanRemoval = true)
	private List<UserFavor> userFavorList = new ArrayList<>();

	@OneToMany(mappedBy = "follower", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
	private List<Follow> followingList = new ArrayList<>();

	@Builder
	public User(String nickname, String email, String password, String address, Gender gender, int age, Role role,
		Level level, Integer postingCount, Integer point, Integer follower, Integer following) {
		this.nickname = nickname;
		this.email = email;
		this.password = password;
		this.address = address;
		this.gender = gender;
		this.age = age;
		this.role = role;
		this.level = level != null ? level : Level.NORMAL;
		this.postingCount = postingCount != null ? postingCount : 0;
		this.point = point != null ? point : 0;
		this.follower = follower != null ? follower : 0;
		this.following = following != null ? following : 0;
	}

	@Builder
	public User(SignupRequestDto requestDto) {
		this.nickname = requestDto.getNickname();
		this.email = requestDto.getEmail();
		this.password = requestDto.getPassword();
		this.address = requestDto.getAddress();
		this.gender = requestDto.getGender() != null ?
			Gender.valueOf(requestDto.getGender()) : null;
		this.age = requestDto.getAge() != null ? requestDto.getAge() : 0;
		this.role = requestDto.getRole() != null ?
			Role.valueOf(requestDto.getRole()) : Role.USER;
		this.level = Level.NORMAL;
		this.postingCount = 0;
		this.point = 0;
		this.follower = 0;
		this.following = 0;
	}

	// 비밀번호 검증 후 업데이트
	public void update(UserUpdateRequestDto requestDto) {
		if (requestDto.getNewPassword() != null) {
			this.password = requestDto.getNewPassword();        // encoded password
		}
		if (requestDto.getNickname() != null) {
			this.nickname = requestDto.getNickname();        // TODO: UNIQUE 걸건지?
		}
		if (requestDto.getAddress() != null) {
			this.address = requestDto.getAddress();
		}
	}

	public void follow(User follower, User following) {
		this.followingList.add(new Follow(follower, following));
		this.following++;
	}

	public void followed() {
		this.follower++;
	}

	public void unfollow(Follow follow) {
		this.followingList.remove(follow);
		this.following--;
	}

	public void unfollowed() {
		this.follower--;
	}

	public void increasePoint(int point) {
		if (this.point > Integer.MAX_VALUE - point) {
			throw new CustomException(PK_POINT_OVERFLOW);
		}
		this.point += point;
	}

	public void removeUserFavorList(List<UserFavor> userFavorList) {
		for (UserFavor userFavor : userFavorList) {
			this.userFavorList.remove(userFavor);
			userFavor.remove();
		}
	}
}
