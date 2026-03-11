package com.workout.app.domain.user;

/**
 * [사용자 권한 Enum]
 *
 * Spring Security는 권한명이 "ROLE_" 로 시작해야 합니다.
 * hasRole("USER") 체크 시 내부적으로 "ROLE_USER"와 비교합니다.
 */
public enum Role {
    ROLE_USER,   // 일반 사용자
    ROLE_ADMIN   // 관리자 (향후 확장용)
}
