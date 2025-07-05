package com.example.taste.fixtures;

import java.util.List;

import com.example.taste.domain.favor.entity.Favor;
import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.entity.UserFavor;

public class FavorFixture {
	public static List<String> favorList = List.of("단짠단짠", "부먹", "찍먹", "뜯먹");

	public static List<Favor> createFavorList() {
		return favorList.stream()
			.map(Favor::new)
			.toList();
	}

	public static List<UserFavor> createUserFavorList(User user) {
		return createFavorList().stream()
			.map(f -> new UserFavor(user, f))
			.toList();
	}
}
