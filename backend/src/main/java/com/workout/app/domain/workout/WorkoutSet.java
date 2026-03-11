package com.workout.app.domain.workout;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

/**
 * [WorkoutSet - 세트 기록]
 *
 * 운동 항목(WorkoutExercise)의 각 세트별 기록입니다.
 * 세트마다 무게와 횟수를 다르게 기록할 수 있습니다.
 *
 * 예시:
 *   벤치프레스 1세트: 60kg x 12회
 *   벤치프레스 2세트: 70kg x 10회
 *   벤치프레스 3세트: 80kg x 8회
 *   푸시업 1세트: 무게없음(null) x 20회
 */
@Entity
@Table(name = "workout_sets")
@Getter
@Setter(AccessLevel.PACKAGE) // WorkoutExercise.addSet()에서만 설정 가능
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorkoutSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어느 운동 항목의 세트인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_exercise_id", nullable = false)
    private WorkoutExercise workoutExercise;

    // 세트 번호 (1, 2, 3...)
    @Column(nullable = false)
    private Integer setNumber;

    // 무게 (kg 단위, 소수점 2자리까지)
    // nullable = true: 맨몸운동은 무게가 없으므로 null 허용
    // DECIMAL(5,2): 최대 999.99kg까지 저장 가능
    // [주의] Double에 scale=2 쓰면 Hibernate 6에서 오류 → BigDecimal 사용
    @Column(precision = 5, scale = 2)
    private BigDecimal weightKg;

    // 반복 횟수
    @Column(nullable = false)
    private Integer reps;

    // 세트 완료 여부 (계획만 세우고 실제로 못 한 세트 구분용)
    @Column(nullable = false)
    private Boolean completed = true;

    @Builder
    public WorkoutSet(WorkoutExercise workoutExercise, Integer setNumber,
                      BigDecimal weightKg, Integer reps, Boolean completed) {
        this.workoutExercise = workoutExercise;
        this.setNumber = setNumber;
        this.weightKg = weightKg;
        this.reps = reps;
        this.completed = completed != null ? completed : true;
    }

    // 세트 수정 (무게, 횟수 변경)
    public void update(BigDecimal weightKg, Integer reps, Boolean completed) {
        this.weightKg = weightKg;
        this.reps = reps;
        this.completed = completed;
    }
}