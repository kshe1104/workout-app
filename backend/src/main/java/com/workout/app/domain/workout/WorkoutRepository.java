package com.workout.app.domain.workout;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * [WorkoutRepository - 운동 기록 데이터 접근]
 *
 * 달력 조회와 상세 조회 두 가지 핵심 쿼리가 있습니다.
 */
public interface WorkoutRepository extends JpaRepository<WorkoutLog, Long> {

    /**
     * 특정 날짜의 운동 기록 상세 조회
     *
     * [fetch join 설명]
     * 일반 조회: WorkoutLog 조회 → exercises 접근 시 추가 쿼리 → sets 접근 시 또 추가 쿼리
     *           → N+1 문제 발생!
     *
     * fetch join 사용: WorkoutLog + exercises + sets + exercise정보를 한 번의 쿼리로!
     *           → 성능 최적화
     *
     * JOIN FETCH: LEFT JOIN이 아닌 INNER JOIN이므로 exercises가 없는 날은 조회 안 됨
     * LEFT JOIN FETCH: exercises가 없어도 WorkoutLog는 조회됨 (메모만 있는 날)
     */
    @Query("SELECT DISTINCT wl FROM WorkoutLog wl " +
           "LEFT JOIN FETCH wl.exercises we " +           // 운동 항목 함께 조회
           "LEFT JOIN FETCH we.exercise e " +             // 운동 종목 정보
           "LEFT JOIN FETCH e.bodyPart " +                // 운동 부위 정보
           "LEFT JOIN FETCH we.sets " +                   // 세트 기록
           "WHERE wl.user.id = :userId " +
           "AND wl.workoutDate = :date")
    Optional<WorkoutLog> findByUserIdAndDateWithDetails(
            @Param("userId") Long userId,
            @Param("date") LocalDate date);

    /**
     * 월별 달력용 운동 기록 조회 (운동한 날짜 목록)
     *
     * 달력에는 "운동했냐 안했냐"와 "어느 부위 했냐"만 필요합니다.
     * 세트 정보까지 전부 가져오면 데이터가 너무 많아지므로
     * 날짜와 부위 정보만 가져오는 최적화된 쿼리를 사용합니다.
     *
     * YEAR(), MONTH(): MySQL 날짜 함수 → 연/월로 필터링
     */
    @Query("SELECT DISTINCT wl FROM WorkoutLog wl " +
           "LEFT JOIN FETCH wl.exercises we " +
           "LEFT JOIN FETCH we.exercise e " +
           "LEFT JOIN FETCH e.bodyPart " +
           "WHERE wl.user.id = :userId " +
           "AND YEAR(wl.workoutDate) = :year " +
           "AND MONTH(wl.workoutDate) = :month " +
           "ORDER BY wl.workoutDate ASC")
    List<WorkoutLog> findByUserIdAndYearMonth(
            @Param("userId") Long userId,
            @Param("year") int year,
            @Param("month") int month);

    // 특정 날짜에 이미 기록이 있는지 체크 (중복 생성 방지)
    boolean existsByUserIdAndWorkoutDate(Long userId, LocalDate workoutDate);
}
