package com.example.taste.domain.user.enums;

import com.example.taste.common.exception.CustomException;
import com.example.taste.common.exception.ErrorCode;
import java.util.Arrays;

public enum Role {
    USER, ADMIN;

    public static Role of(String role) {
        return Arrays.stream(Role.values())
            .filter(r -> r.name().equalsIgnoreCase(role))
            .findFirst()
            .orElseThrow(() -> new CustomException(ErrorCode.INVALID_ROLE));
    }
}
