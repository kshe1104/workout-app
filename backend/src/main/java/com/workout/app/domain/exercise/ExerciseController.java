package com.workout.app.domain.exercise;

import com.workout.app.security.CustomUserPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * [ExerciseController - 운동 종목 API]
 *
 * GET  /api/exercises              → 전체 종목 조회 (기본 + 커스텀)
 * GET  /api/exercises?bodyPartId=1 → 부위별 종목 조회
 * GET  /api/exercises/body-parts   → 운동 부위 목록 조회
 * POST /api/exercises              → 커스텀 종목 추가
 * DELETE /api/exercises/{id}       → 커스텀 종목 삭제
 */
@RestController
@RequestMapping("/api/exercises")
@RequiredArgsConstructor
public class ExerciseController {

    private final ExerciseService exerciseService;

    /**
     * 운동 종목 조회
     * bodyPartId 파라미터가 있으면 해당 부위만, 없으면 전체 조회
     *
     * @RequestParam(required = false): 쿼리 파라미터가 없어도 오류 안 남 (null로 처리)
     */
    @GetMapping
    public ResponseEntity<List<ExerciseService.ExerciseResult>> getExercises(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam(required = false) Long bodyPartId) {

        return ResponseEntity.ok(
                exerciseService.getExercises(principal.getId(), bodyPartId)
        );
    }

    /**
     * 운동 부위 목록 조회
     * 프론트의 부위 선택 탭에 사용
     */
    @GetMapping("/body-parts")
    public ResponseEntity<List<ExerciseService.BodyPartResult>> getBodyParts() {
        return ResponseEntity.ok(exerciseService.getBodyParts());
    }

    /**
     * 커스텀 운동 종목 추가
     * POST /api/exercises
     * Body: { "name": "내 커스텀 운동", "bodyPartId": 1 }
     */
    @PostMapping
    public ResponseEntity<ExerciseService.ExerciseResult> addExercise(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody AddExerciseRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED).body(
                exerciseService.addCustomExercise(
                        principal.getId(), request.getName(), request.getBodyPartId())
        );
    }

    /**
     * 커스텀 운동 종목 삭제
     * DELETE /api/exercises/42
     */
    @DeleteMapping("/{exerciseId}")
    public ResponseEntity<Void> deleteExercise(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long exerciseId) {

        exerciseService.deleteCustomExercise(principal.getId(), exerciseId);
        return ResponseEntity.noContent().build();
    }

    // 요청 DTO
    @Getter
    static class AddExerciseRequest {
        @NotBlank(message = "운동 이름은 필수입니다")
        @Size(max = 100, message = "운동 이름은 100자 이내여야 합니다")
        private String name;

        @NotNull(message = "운동 부위는 필수입니다")
        private Long bodyPartId;
    }
}
