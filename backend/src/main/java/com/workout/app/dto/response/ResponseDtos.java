package com.workout.app.dto.response;

import com.workout.app.domain.workout.WorkoutLog;
import com.workout.app.domain.workout.WorkoutExercise;
import com.workout.app.domain.workout.WorkoutSet;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * [Response DTO들]
 *
 * Entity → DTO 변환 시 주의사항:
 * Entity를 그대로 JSON으로 직렬화하면 안 됩니다.
 * 이유 1: 양방향 연관관계에서 무한 루프 발생 (User → WorkoutLog → User → ...)
 * 이유 2: 불필요한 데이터까지 노출
 * 이유 3: 지연 로딩(LAZY) Entity를 직렬화하면 LazyInitializationException 발생
 *
 * → 항상 Service에서 Entity를 DTO로 변환 후 Controller에서 반환
 */

// ── 로그인 응답 ────────────────────────────────────────────────────
@Getter
@Builder
class TokenResponse {
    private String accessToken;
    private String refreshToken;
    private String email;
    private String nickname;
}

// ── 달력 응답 (월별 운동 여부) ─────────────────────────────────────
@Getter
@Builder
class CalendarResponse {
    private int year;
    private int month;
    private List<CalendarDayDto> workoutDays;

    @Getter
    @Builder
    static class CalendarDayDto {
        private LocalDate date;
        private boolean hasWorkout;
        // 어느 부위를 운동했는지 (달력에 색상으로 표시용)
        private List<String> bodyParts;
    }
}

// ── 운동 기록 상세 응답 ────────────────────────────────────────────
@Getter
@Builder
class WorkoutDetailResponse {
    private LocalDate date;
    private String memo;
    private List<ExerciseDetailDto> exercises;

    // Entity → DTO 변환 정적 팩토리 메서드
    // 이렇게 DTO 안에 변환 메서드를 두면 Service 코드가 깔끔해집니다
    public static WorkoutDetailResponse from(WorkoutLog workoutLog) {
        List<ExerciseDetailDto> exerciseDtos = workoutLog.getExercises()
                .stream()
                .map(ExerciseDetailDto::from)
                .collect(Collectors.toList());

        return WorkoutDetailResponse.builder()
                .date(workoutLog.getWorkoutDate())
                .memo(workoutLog.getMemo())
                .exercises(exerciseDtos)
                .build();
    }

    @Getter
    @Builder
    static class ExerciseDetailDto {
        private Long workoutExerciseId;
        private String exerciseName;
        private String bodyPart;
        private Integer orderIndex;
        private List<SetDetailDto> sets;

        public static ExerciseDetailDto from(WorkoutExercise workoutExercise) {
            List<SetDetailDto> setDtos = workoutExercise.getSets()
                    .stream()
                    .map(SetDetailDto::from)
                    .collect(Collectors.toList());

            return ExerciseDetailDto.builder()
                    .workoutExerciseId(workoutExercise.getId())
                    .exerciseName(workoutExercise.getExercise().getName())
                    .bodyPart(workoutExercise.getExercise().getBodyPart().getName())
                    .orderIndex(workoutExercise.getOrderIndex())
                    .sets(setDtos)
                    .build();
        }
    }

    @Getter
    @Builder
    static class SetDetailDto {
        private Long setId;
        private Integer setNumber;
        private BigDecimal weightKg;  // null이면 맨몸운동
        private Integer reps;
        private Boolean completed;

        public static SetDetailDto from(WorkoutSet set) {
            return SetDetailDto.builder()
                    .setId(set.getId())
                    .setNumber(set.getSetNumber())
                    .weightKg(set.getWeightKg())
                    .reps(set.getReps())
                    .completed(set.getCompleted())
                    .build();
        }
    }
}

// ── 공통 API 응답 래퍼 ────────────────────────────────────────────
/**
 * 모든 API 응답을 일관된 형태로 만들기 위한 래퍼 클래스
 *
 * 성공 응답:
 * {
 *   "success": true,
 *   "message": "운동 기록이 저장되었습니다",
 *   "data": { ... }
 * }
 *
 * 실패 응답:
 * {
 *   "success": false,
 *   "message": "이미 해당 날짜에 기록이 존재합니다",
 *   "data": null
 * }
 */
@Getter
@Builder
class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;

    // 성공 응답 생성 편의 메서드
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    // 실패 응답 생성 편의 메서드
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .build();
    }
}