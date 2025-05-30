package com.example.taste.domain.user.entity;

import com.example.taste.common.entity.SoftDeletableEntity;
import com.example.taste.domain.user.enums.Gender;
import com.example.taste.domain.user.enums.Level;
import com.example.taste.domain.user.enums.Role;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public class User extends SoftDeletableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nickname;
    private String email;
    private String password;
    private String address;
    private Gender gender;
    private int age;
    private Role role;

    @Builder.Default
    private Level level = Level.NORMAL;

    @Builder.Default
    private int postingCount = 0;

    @Builder.Default
    private int point = 0;

    @Builder.Default
    private int follower = 0;

    @Builder.Default
    private int following = 0;
}
