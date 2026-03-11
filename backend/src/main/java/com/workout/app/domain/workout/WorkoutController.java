package com.workout.app.domain.workout;

import com.workout.app.security.CustomUserPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * [WorkoutController - 운동 기록 API]
 *
 * 모든 API는 JWT 인증이 필요합니다. (SecurityConfig에서 anyRequest().authenticated() 설정)
 * @AuthenticationPrincipal로 현재 로그인한 사용자 정보를 받아서
 * 본인의 데이터만 조회/수정할 수 있도록 합니다.
 */
@RestController
@RequestMapping("/api/workouts")
@RequiredArgsConstructor
public class WorkoutController {

    private final WorkoutService workoutService;

    /**
     * 월별 달력 조회
     * GET /api/workouts/calendar?year=2025&month=3
     *
     * @RequestParam: URL 쿼리 파라미터 (?year=2025&month=3) 추출
     */
    @GetMapping("/calendar")
    public ResponseEntity<WorkoutService.CalendarResult> getCalendar(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam int year,
            @RequestParam int month) {

        WorkoutService.CalendarResult result =
                workoutService.getCalendar(principal.getId(), year, month);
        return ResponseEntity.ok(result);
    }

    /**
     * 특정 날짜 운동 기록 상세 조회
     * GET /api/workouts/2025-03-10
     *
     * @PathVariable: URL 경로의 {date} 부분 추출
     * @DateTimeFormat: 문자열 "2025-03-10"을 LocalDate 객체로 자동 변환
     */
    @GetMapping("/{date}")
    public ResponseEntity<WorkoutService.WorkoutDetailResult> getWorkoutDetail(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        WorkoutService.WorkoutDetailResult result =
                workoutService.getWorkoutDetail(principal.getId(), date);
        return ResponseEntity.ok(result);
    }

    /**
     * 운동 기록 저장
     * POST /api/workouts
     */
    @PostMapping
    public ResponseEntity<Map<String, Long>> saveWorkout(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody WorkoutSaveRequest request) {

        Long workoutId = workoutService.saveWorkout(
                principal.getId(),
                request.getDate(),
                request.getMemo(),
                request.getExercises().stream()
                        .map(e -> new WorkoutService.ExerciseSaveRequest(
                                e.getExerciseId(),
                                e.getOrderIndex(),
                                e.getSets().stream()
                                        .map(s -> new WorkoutService.SetSaveRequest(
                                                s.getSetNumber(), s.getWeightKg(),
                                                s.getReps(), s.getCompleted()))
                                        .toList()
                        ))
                        .toList()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("workoutId", workoutId));
    }

    /**
     * 운동 기록 수정
     * PUT /api/workouts/2025-03-10
     */
    @PutMapping("/{date}")
    public ResponseEntity<Void> updateWorkout(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Valid @RequestBody WorkoutSaveRequest request) {

        workoutService.updateWorkout(
                principal.getId(), date, request.getMemo(),
                request.getExercises().stream()
                        .map(e -> new WorkoutService.ExerciseSaveRequest(
                                e.getExerciseId(), e.getOrderIndex(),
                                e.getSets().stream()
                                        .map(s -> new WorkoutService.SetSaveRequest(
                                                s.getSetNumber(), s.getWeightKg(),
                                                s.getReps(), s.getCompleted()))
                                        .toList()))
                        .toList()
        );

        return ResponseEntity.ok().build();
    }

    /**
     * 운동 기록 삭제
     * DELETE /api/workouts/2025-03-10
     */
    @DeleteMapping("/{date}")
    public ResponseEntity<Void> deleteWorkout(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        workoutService.deleteWorkout(principal.getId(), date);
        return ResponseEntity.noContent().build();
    }

    // ── 요청 DTO ──────────────────────────────────────────────────────

    @Getter
    static class WorkoutSaveRequest {
        @NotNull
        private LocalDate date;
        private String memo;
        @NotEmpty
        private List<@Valid ExerciseRequest> exercises;
    }

    @Getter
    static class ExerciseRequest {
        @NotNull
        private Long exerciseId;
        @NotNull
        private Integer orderIndex;
        @NotEmpty
        private List<@Valid SetRequest> sets;
    }

    @Getter
    static class SetRequest {
        @NotNull @Min(1)
        private Integer setNumber;
        private BigDecimal weightKg; // null 허용 (맨몸운동)
        @NotNull @Min(1)
        private Integer reps;
        private Boolean completed = true;
    }
}