package com.example.taste.domain.user.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.taste.domain.user.enums.Role;

@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails, Serializable {
	private final Long id;
	private final String email;
	private final String password;
	private final Role role;
	private final LocalDateTime deletedAt;

	@Builder
	public CustomUserDetails(User user) {
		this.id = user.getId();
		this.email = user.getEmail();
		this.password = user.getPassword();
		this.role = user.getRole();
		this.deletedAt = user.getDeletedAt();
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority("ROLE_" + getRole()));
	}

	@Override
	public String getPassword() {
		return getPassword();
	}

	// 이메일 식별자
	@Override
	public String getUsername() {
		return getEmail();
	}

	public Long getId() {
		return getId();
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {        // 계정 활성화 여부
		return getDeletedAt() == null;
	}
}
