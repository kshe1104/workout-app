package com.workout.app.dto.request;

import jakarta.validation.Valid;                 // ← @Valid 어노테이션 import (이게 없어서 빌드 오류났음)
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * [DTO (Data Transfer Object)란?]
 *
 * Entity를 직접 외부에 노출하면 안 됩니다:
 * 1. 보안: password 같은 민감한 정보가 노출될 수 있음
 * 2. 결합도: API 스펙이 DB 구조에 종속됨
 * 3. 검증: Entity에 API 입력 검증 로직이 섞임
 *
 * → 요청(Request)과 응답(Response)마다 별도의 DTO 클래스를 사용합니다.
 */

// ── 회원가입 요청 ──────────────────────────────────────────────────
@Getter
@NoArgsConstructor
class SignupRequest {

    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다")
    private String password;

    @NotBlank(message = "닉네임은 필수입니다")
    @Size(min = 2, max = 20, message = "닉네임은 2~20자 사이여야 합니다")
    private String nickname;
}

// ── 로그인 요청 ────────────────────────────────────────────────────
@Getter
@NoArgsConstructor
class LoginRequest {

    @NotBlank(message = "이메일은 필수입니다")
    @Email
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다")
    private String password;
}

// ── 운동 기록 저장 요청 ────────────────────────────────────────────
@Getter
@NoArgsConstructor
class WorkoutSaveRequest {

    @NotNull(message = "날짜는 필수입니다")
    private LocalDate date;

    private String memo;

    // @Valid: exercises 리스트 안의 객체들도 검증 실행
    // List<@Valid ...> 형태로 사용하려면 jakarta.validation.Valid import 필수
    @NotEmpty(message = "운동 항목은 최소 1개 이상이어야 합니다")
    private List<@Valid WorkoutExerciseRequest> exercises;
}

// ── 운동 항목 요청 ─────────────────────────────────────────────────
@Getter
@NoArgsConstructor
class WorkoutExerciseRequest {

    @NotNull(message = "운동 종목 ID는 필수입니다")
    private Long exerciseId;

    @NotNull(message = "순서는 필수입니다")
    private Integer orderIndex;

    @NotEmpty(message = "세트는 최소 1개 이상이어야 합니다")
    private List<@Valid WorkoutSetRequest> sets;
}

// ── 세트 요청 ──────────────────────────────────────────────────────
@Getter
@NoArgsConstructor
class WorkoutSetRequest {

    @NotNull(message = "세트 번호는 필수입니다")
    @Min(value = 1, message = "세트 번호는 1 이상이어야 합니다")
    private Integer setNumber;

    // 무게는 null 허용 (맨몸운동)
    @DecimalMin(value = "0.0", message = "무게는 0 이상이어야 합니다")
    private Double weightKg;

    @NotNull(message = "횟수는 필수입니다")
    @Min(value = 1, message = "횟수는 1 이상이어야 합니다")
    private Integer reps;

    private Boolean completed = true;
}

// ── 커스텀 운동 종목 추가 요청 ─────────────────────────────────────
@Getter
@NoArgsConstructor
class CustomExerciseRequest {

    @NotBlank(message = "운동 이름은 필수입니다")
    @Size(max = 100)
    private String name;

    @NotNull(message = "운동 부위는 필수입니다")
    private Long bodyPartId;
}
