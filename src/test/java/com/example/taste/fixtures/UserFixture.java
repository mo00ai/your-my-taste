package com.example.taste.fixtures;

import static com.example.taste.domain.user.enums.Gender.*;

import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.enums.Level;
import com.example.taste.domain.user.enums.Role;

public class UserFixture {
	public static User create() {
		return User.builder()
			.nickname("testUser")
			.email("testUser@example.com")
			.password("encoded-password")
			.address("서울특별시 00구 00동")
			.gender(ANY)
			.age(20)
			.role(Role.USER)
			.level(Level.NORMAL)
			.postingCount(0)
			.point(0)
			.follower(0)
			.following(0)
			.build();
	}
}
