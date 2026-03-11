package com.workout.app.domain.workout;

import com.workout.app.domain.exercise.Exercise;
import com.workout.app.domain.exercise.ExerciseRepository;
import com.workout.app.domain.user.User;
import com.workout.app.domain.user.UserRepository;
import com.workout.app.exception.BusinessException;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * [WorkoutService - 운동 기록 비즈니스 로직]
 *
 * Service 계층의 역할:
 * - 비즈니스 규칙 적용 (날짜 중복 체크, 본인 기록만 접근 가능 등)
 * - 여러 Repository를 조합해서 데이터 처리
 * - 트랜잭션 관리
 * - Entity → Response DTO 변환
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkoutService {

    private final WorkoutRepository workoutRepository;
    private final UserRepository userRepository;
    private final ExerciseRepository exerciseRepository;

    /**
     * 월별 달력 데이터 조회
     * 해당 월에 운동한 날짜와 운동 부위 목록 반환
     */
    public CalendarResult getCalendar(Long userId, int year, int month) {
        List<WorkoutLog> logs = workoutRepository.findByUserIdAndYearMonth(userId, year, month);

        List<CalendarDayResult> days = logs.stream()
                .map(log -> {
                    // 해당 날에 운동한 부위들 추출 (중복 제거)
                    List<String> bodyParts = log.getExercises().stream()
                            .map(we -> we.getExercise().getBodyPart().getName())
                            .distinct()
                            .collect(Collectors.toList());

                    return CalendarDayResult.builder()
                            .date(log.getWorkoutDate())
                            .hasWorkout(true)
                            .bodyParts(bodyParts)
                            .build();
                })
                .collect(Collectors.toList());

        return new CalendarResult(year, month, days);
    }

    /**
     * 특정 날짜 운동 기록 상세 조회
     */
    public WorkoutDetailResult getWorkoutDetail(Long userId, LocalDate date) {
        WorkoutLog log = workoutRepository
                .findByUserIdAndDateWithDetails(userId, date)
                .orElseThrow(() -> BusinessException.notFound("해당 날짜의 운동 기록이 없습니다"));

        return WorkoutDetailResult.from(log);
    }

    /**
     * 운동 기록 저장
     *
     * 저장 순서:
     * 1. 날짜 중복 체크
     * 2. WorkoutLog 생성 (날짜 헤더)
     * 3. WorkoutExercise 생성 (운동 종목들)
     * 4. WorkoutSet 생성 (세트 기록들)
     * 5. 저장 (cascade로 연관 엔티티 함께 저장)
     */
    @Transactional
    public Long saveWorkout(Long userId, LocalDate date, String memo,
                            List<ExerciseSaveRequest> exerciseRequests) {
        // 중복 체크
        if (workoutRepository.existsByUserIdAndWorkoutDate(userId, date)) {
            throw BusinessException.conflict("해당 날짜에 이미 운동 기록이 있습니다");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("사용자를 찾을 수 없습니다"));

        // WorkoutLog 생성 (날짜 헤더)
        WorkoutLog workoutLog = WorkoutLog.builder()
                .user(user)
                .workoutDate(date)
                .memo(memo)
                .build();

        // 운동 항목들 생성 및 추가
        // record의 접근자는 get 접두사 없이 필드명 그대로 사용 (exerciseId(), orderIndex()...)
        for (ExerciseSaveRequest req : exerciseRequests) {
            Exercise exercise = exerciseRepository.findById(req.exerciseId())
                    .orElseThrow(() -> BusinessException.notFound(
                            "운동 종목을 찾을 수 없습니다: " + req.exerciseId()));

            WorkoutExercise workoutExercise = WorkoutExercise.builder()
                    .workoutLog(workoutLog)
                    .exercise(exercise)
                    .orderIndex(req.orderIndex())
                    .build();

            // 세트 기록들 추가
            for (SetSaveRequest setReq : req.sets()) {
                WorkoutSet set = WorkoutSet.builder()
                        .workoutExercise(workoutExercise)
                        .setNumber(setReq.setNumber())
                        .weightKg(setReq.weightKg())
                        .reps(setReq.reps())
                        .completed(setReq.completed())
                        .build();
                workoutExercise.addSet(set); // 편의 메서드로 추가
            }

            workoutLog.addExercise(workoutExercise); // 편의 메서드로 추가
        }

        // workoutLog 저장 시 cascade = ALL로 설정했으므로
        // WorkoutExercise, WorkoutSet도 함께 자동 저장됩니다
        WorkoutLog saved = workoutRepository.save(workoutLog);
        return saved.getId();
    }

    /**
     * 운동 기록 수정
     * 기존 운동 항목 모두 삭제 후 새로 저장 (전체 교체 방식)
     *
     * 왜 전체 교체 방식을 쓰나?
     * 세트 추가/삭제/수정을 개별로 처리하면 복잡도가 매우 높아집니다.
     * 운동 기록 수정은 빈번하지 않으므로 전체 교체가 실용적입니다.
     */
    @Transactional
    public void updateWorkout(Long userId, LocalDate date, String memo,
                              List<ExerciseSaveRequest> exerciseRequests) {
        WorkoutLog workoutLog = workoutRepository
                .findByUserIdAndDateWithDetails(userId, date)
                .orElseThrow(() -> BusinessException.notFound("운동 기록을 찾을 수 없습니다"));

        // 본인 기록인지 확인 (다른 사람 기록 수정 방지)
        if (!workoutLog.getUser().getId().equals(userId)) {
            throw BusinessException.badRequest("본인의 운동 기록만 수정할 수 있습니다");
        }

        workoutLog.updateMemo(memo);

        // 기존 운동 항목 모두 제거
        // orphanRemoval = true 설정으로 DB에서도 자동 삭제됨
        workoutLog.getExercises().clear();

        // 새 운동 항목 추가 (saveWorkout과 동일한 로직)
        for (ExerciseSaveRequest req : exerciseRequests) {
            Exercise exercise = exerciseRepository.findById(req.exerciseId())
                    .orElseThrow(() -> BusinessException.notFound("운동 종목을 찾을 수 없습니다"));

            WorkoutExercise workoutExercise = WorkoutExercise.builder()
                    .workoutLog(workoutLog)
                    .exercise(exercise)
                    .orderIndex(req.orderIndex())
                    .build();

            for (SetSaveRequest setReq : req.sets()) {
                WorkoutSet set = WorkoutSet.builder()
                        .workoutExercise(workoutExercise)
                        .setNumber(setReq.setNumber())
                        .weightKg(setReq.weightKg())
                        .reps(setReq.reps())
                        .completed(setReq.completed())
                        .build();
                workoutExercise.addSet(set);
            }
            workoutLog.addExercise(workoutExercise);
        }
        // @Transactional: 메서드 종료 시 dirty checking으로 자동 UPDATE 쿼리 실행
    }

    /**
     * 운동 기록 삭제
     */
    @Transactional
    public void deleteWorkout(Long userId, LocalDate date) {
        WorkoutLog workoutLog = workoutRepository
                .findByUserIdAndDateWithDetails(userId, date)
                .orElseThrow(() -> BusinessException.notFound("운동 기록을 찾을 수 없습니다"));

        if (!workoutLog.getUser().getId().equals(userId)) {
            throw BusinessException.badRequest("본인의 운동 기록만 삭제할 수 있습니다");
        }

        // cascade = ALL이므로 WorkoutExercise, WorkoutSet도 함께 삭제
        workoutRepository.delete(workoutLog);
    }

    // ── 내부에서 사용하는 Request/Result 레코드들 ──────────────────────

    public record CalendarResult(int year, int month, List<CalendarDayResult> days) {}

    @Getter @Builder
    public static class CalendarDayResult {
        private LocalDate date;
        private boolean hasWorkout;
        private List<String> bodyParts;
    }

    // WorkoutDetailResult는 Entity에서 직접 변환
    public record WorkoutDetailResult(LocalDate date, String memo,
                                      List<ExerciseDetailResult> exercises) {
        public static WorkoutDetailResult from(WorkoutLog log) {
            return new WorkoutDetailResult(
                    log.getWorkoutDate(),
                    log.getMemo(),
                    log.getExercises().stream()
                            .map(ExerciseDetailResult::from)
                            .toList()
            );
        }
    }

    public record ExerciseDetailResult(Long id, String name, String bodyPart,
                                       int orderIndex, List<SetDetailResult> sets) {
        public static ExerciseDetailResult from(WorkoutExercise we) {
            return new ExerciseDetailResult(
                    we.getId(),
                    we.getExercise().getName(),
                    we.getExercise().getBodyPart().getName(),
                    we.getOrderIndex(),
                    we.getSets().stream().map(SetDetailResult::from).toList()
            );
        }
    }

    public record SetDetailResult(Long id, int setNumber, BigDecimal weightKg,
                                  int reps, boolean completed) {
        public static SetDetailResult from(WorkoutSet set) {
            return new SetDetailResult(
                    set.getId(), set.getSetNumber(), set.getWeightKg(),
                    set.getReps(), set.getCompleted()
            );
        }
    }

    // 저장 요청용 레코드
    public record ExerciseSaveRequest(Long exerciseId, Integer orderIndex,
                                      List<SetSaveRequest> sets) {}
    public record SetSaveRequest(Integer setNumber, BigDecimal weightKg,
                                 Integer reps, Boolean completed) {}
}