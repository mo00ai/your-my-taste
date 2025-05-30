package com.example.taste.domain.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Builder;
import lombok.Getter;

import com.example.taste.common.entity.SoftDeletableEntity;
import com.example.taste.domain.user.enums.Gender;
import com.example.taste.domain.user.enums.Level;
import com.example.taste.domain.user.enums.Role;

@Getter
@Entity
@Table(name = "users")
public class User extends SoftDeletableEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// TODO: 이미지 연관관계 추가

	@Column(nullable = false)
	private String nickname;

	@Column(nullable = false)
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
		this.level = level == null ? Level.NORMAL : level;
		this.postingCount = postingCount == null ? postingCount : 0;
		this.point = point == null ? point : 0;
		this.follower = follower == null ? point : 0;
		this.following = following == null ? point : 0;
	}
}