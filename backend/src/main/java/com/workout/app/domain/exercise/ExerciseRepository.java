package com.workout.app.domain.exercise;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExerciseRepository extends JpaRepository<Exercise, Long> {

    /**
     * 사용자가 볼 수 있는 운동 종목 조회
     * - 기본 종목 (user = null): 모든 사용자에게 보임
     * - 커스텀 종목 (user = 해당 사용자): 본인만 볼 수 있음
     *
     * JOIN FETCH e.bodyPart: 부위 정보를 함께 가져와서 N+1 방지
     */
    @Query("SELECT e FROM Exercise e " +
           "JOIN FETCH e.bodyPart " +
           "WHERE e.user IS NULL OR e.user.id = :userId " +
           "ORDER BY e.bodyPart.id ASC, e.name ASC")
    List<Exercise> findAvailableExercises(@Param("userId") Long userId);

    /**
     * 특정 부위의 운동 종목만 조회 (부위별 필터링)
     */
    @Query("SELECT e FROM Exercise e " +
           "JOIN FETCH e.bodyPart bp " +
           "WHERE (e.user IS NULL OR e.user.id = :userId) " +
           "AND bp.id = :bodyPartId")
    List<Exercise> findByBodyPartAndUser(
            @Param("userId") Long userId,
            @Param("bodyPartId") Long bodyPartId);
}
