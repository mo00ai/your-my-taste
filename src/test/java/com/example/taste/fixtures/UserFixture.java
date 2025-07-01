package com.example.taste.fixtures;

import static com.example.taste.domain.user.enums.Gender.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.example.taste.domain.image.entity.Image;
import com.example.taste.domain.image.enums.ImageType;
import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.enums.Level;
import com.example.taste.domain.user.enums.Role;

public class UserFixture {
	public static User create(Image image) {
		User user = User.builder()
			.nickname("testUser")
			.email("testUser+" + UUID.randomUUID() + "@example.com")
			.password("password")
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

		user.setImage(image);
		return user;
	}

	public static User createWithEncodedPw(Image image, String pw) {
		User user = User.builder()
			.nickname("testUser")
			.email("testUser+" + UUID.randomUUID() + "@example.com")
			.password(pw)
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

		user.setImage(image);
		return user;
	}

	public static User createNoMorePosting(Image image) {
		User user = User.builder()
			.nickname("testUser")
			.email("testUser+" + UUID.randomUUID() + "@example.com")
			.password("encoded-password")
			.address("서울특별시 00구 00동")
			.gender(ANY)
			.age(20)
			.role(Role.USER)
			.level(Level.NORMAL)
			.postingCount(Level.NORMAL.getPostingLimit())
			.point(0)
			.follower(0)
			.following(0)
			.build();

		user.setImage(image);
		return user;
	}

	public static List<User> createUsers() {
		List<User> users = new ArrayList<>();

		for (int i = 0; i < 10; i++) {
			Image image = Image.builder()
				.type(ImageType.USER)
				.url("testUrl.png")
				.originFileName("original" + i + ".png")
				.uploadFileName("upload" + i + ".png")
				.fileSize(500 * 500L)
				.fileExtension("png")
				.build();

			User user = User.builder()
				.nickname("testUser" + i)
				.email("testUser" + i + "+" + UUID.randomUUID() + "@example.com")
				.password("encoded-password")
				.address("서울특별시 00구 00동")
				.gender(ANY)
				.age(20 + i)
				.role(Role.USER)
				.level(Level.NORMAL)
				.postingCount(0)
				.point(10)
				.follower(0)
				.following(0)
				.build();

			user.setImage(image);
			users.add(user);
		}

		return users;
	}
}
