package com.workout.app.domain.workout;

import com.workout.app.domain.exercise.Exercise;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * [WorkoutExercise - 운동 항목]
 *
 * 하루의 운동 기록(WorkoutLog) 안에 포함된 개별 운동 종목입니다.
 * 예: 2025-03-10 기록에서 "벤치프레스", "스쿼트" 각각이 WorkoutExercise
 *
 * [연관관계]
 * WorkoutExercise (N) ──── (1) WorkoutLog  (어느 날의 기록인지)
 * WorkoutExercise (N) ──── (1) Exercise    (어떤 운동 종목인지)
 * WorkoutExercise (1) ──── (N) WorkoutSet  (세트 기록들)
 */
@Entity
@Table(name = "workout_exercises")
@Getter
@Setter(AccessLevel.PACKAGE) // WorkoutLog.addExercise()에서만 설정 가능하도록 제한
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorkoutExercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어느 날의 운동 기록에 속하는지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_log_id", nullable = false)
    private WorkoutLog workoutLog;

    // 어떤 운동 종목인지 (벤치프레스, 스쿼트 등)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    // 운동 순서 (첫 번째로 한 운동, 두 번째로 한 운동...)
    @Column(nullable = false)
    private Integer orderIndex;

    // 이 운동 종목의 세트 기록들
    // cascade = ALL + orphanRemoval: 세트 추가/삭제가 자동으로 DB에 반영
    @OneToMany(mappedBy = "workoutExercise", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("setNumber ASC") // 1세트, 2세트, 3세트 순으로 정렬
    private Set<WorkoutSet> sets = new LinkedHashSet<>();

    @Builder
    public WorkoutExercise(WorkoutLog workoutLog, Exercise exercise, Integer orderIndex) {
        this.workoutLog = workoutLog;
        this.exercise = exercise;
        this.orderIndex = orderIndex;
    }

    // 세트 추가 편의 메서드
    public void addSet(WorkoutSet set) {
        this.sets.add(set);
        set.setWorkoutExercise(this);
    }
}
