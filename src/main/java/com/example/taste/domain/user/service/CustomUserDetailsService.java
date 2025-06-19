package com.example.taste.domain.user.service;

import static com.example.taste.domain.user.exception.UserErrorCode.DEACTIVATED_USER;
import static com.example.taste.domain.user.exception.UserErrorCode.NOT_FOUND_USER;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.taste.common.exception.CustomException;
import com.example.taste.domain.user.entity.CustomUserDetails;
import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
	private final UserRepository userRepository;

	@Override
	public CustomUserDetails loadUserByUsername(String username)
		throws UsernameNotFoundException {
		User user = userRepository.findUserByEmail(username)
			.orElseThrow(() -> new CustomException(NOT_FOUND_USER));

		if (user.getDeletedAt() != null) {
			throw new CustomException(DEACTIVATED_USER);
		}

		return new CustomUserDetails(user);
	}
}
