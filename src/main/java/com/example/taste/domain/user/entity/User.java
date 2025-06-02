package com.example.taste.domain.user.entity;

import java.util.ArrayList;
import java.util.List;

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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.example.taste.common.entity.SoftDeletableEntity;
import com.example.taste.domain.board.entity.Board;
import com.example.taste.domain.event.entity.Event;
import com.example.taste.domain.image.entity.Image;
import com.example.taste.domain.user.dto.request.UserUpdateRequestDto;
import com.example.taste.domain.user.enums.Gender;
import com.example.taste.domain.user.enums.Level;
import com.example.taste.domain.user.enums.Role;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "users")
public class User extends SoftDeletableEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Setter
	@ManyToOne(fetch = FetchType.LAZY)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JoinColumn(name = "image_id", nullable = false)
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

	@Setter
	@OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
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

	public void unfollow(Follow follow) {
		this.followingList.remove(follow);
		this.following--;
	}
}
