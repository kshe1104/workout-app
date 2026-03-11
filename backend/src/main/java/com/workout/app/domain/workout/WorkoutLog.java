package com.workout.app.domain.workout;

import com.workout.app.domain.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * [WorkoutLog - 운동 기록 헤더]
 *
 * 하루에 하나의 운동 기록이 생성됩니다.
 * 예: 2025-03-10에 운동했다면 WorkoutLog 1개 생성
 *
 * [연관관계 설명]
 * WorkoutLog (1) ──── (N) WorkoutExercise
 * 하나의 날짜에 여러 운동 종목을 기록할 수 있음
 */
@Entity
@Table(
    name = "workout_logs",
    // 같은 유저가 같은 날짜에 기록을 중복 생성하지 못하도록 복합 unique 제약
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "workout_date"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class WorkoutLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @ManyToOne: 여러 WorkoutLog가 하나의 User에 속함
    // @JoinColumn: FK 컬럼명 지정 (user_id)
    // fetch = LAZY: user 정보를 실제로 사용할 때만 DB 조회 (성능 최적화)
    //   → EAGER(즉시 로딩)는 항상 join해서 가져오기 때문에 불필요한 쿼리 발생
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDate workoutDate;

    // 운동 메모 (오늘 컨디션, 특이사항 등)
    @Column(columnDefinition = "TEXT")
    private String memo;

    // @OneToMany: 하나의 WorkoutLog에 여러 WorkoutExercise
    // mappedBy = "workoutLog": WorkoutExercise.workoutLog 필드가 연관관계의 주인
    // cascade = ALL: WorkoutLog 저장/삭제 시 WorkoutExercise도 함께 저장/삭제
    // orphanRemoval = true: 리스트에서 제거된 WorkoutExercise는 DB에서도 삭제
    @OneToMany(mappedBy = "workoutLog", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC") // 운동 순서대로 정렬
    private Set<WorkoutExercise> exercises = new LinkedHashSet<>();

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Builder
    public WorkoutLog(User user, LocalDate workoutDate, String memo) {
        this.user = user;
        this.workoutDate = workoutDate;
        this.memo = memo;
    }

    // 메모 수정
    public void updateMemo(String memo) {
        this.memo = memo;
    }

    // 운동 추가 (연관관계 편의 메서드)
    // 양방향 연관관계에서 양쪽 모두 설정해줘야 함
    public void addExercise(WorkoutExercise exercise) {
        this.exercises.add(exercise);
        exercise.setWorkoutLog(this); // 반대편도 설정
    }
}
