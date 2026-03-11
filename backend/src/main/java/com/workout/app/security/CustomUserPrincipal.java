package com.workout.app.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * [CustomUserPrincipal - 인증된 사용자 정보]
 *
 * Spring Security의 Authentication 객체 안에 들어가는 "사용자 정보"입니다.
 * Controller에서 @AuthenticationPrincipal로 현재 로그인한 사용자 정보를 꺼낼 수 있습니다.
 *
 * 사용 예:
 * @GetMapping("/api/workouts")
 * public ResponseEntity<?> getWorkouts(@AuthenticationPrincipal CustomUserPrincipal user) {
 *     Long userId = user.getId(); // 현재 로그인한 사용자의 ID
 * }
 */
@Getter
@AllArgsConstructor
public class CustomUserPrincipal {
    private Long id;
    private String email;
}
