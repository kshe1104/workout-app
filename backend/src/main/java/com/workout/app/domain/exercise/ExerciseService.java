package com.workout.app.domain.exercise;

import com.workout.app.domain.user.User;
import com.workout.app.domain.user.UserRepository;
import com.workout.app.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * [ExerciseService - 운동 종목 비즈니스 로직]
 *
 * 운동 종목은 두 가지 종류가 있습니다:
 * 1. 기본 종목 (user = null): data.sql로 초기 삽입, 모든 사용자 공통
 * 2. 커스텀 종목 (user = 현재사용자): 사용자가 직접 추가
 *
 * 조회 시 두 가지를 합쳐서 반환합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;
    private final BodyPartRepository bodyPartRepository;
    private final UserRepository userRepository;

    /**
     * 사용자가 사용 가능한 전체 운동 종목 조회
     * 기본 종목 + 본인이 추가한 커스텀 종목
     *
     * @param bodyPartId null이면 전체, 값이 있으면 해당 부위만 필터링
     */
    public List<ExerciseResult> getExercises(Long userId, Long bodyPartId) {
        List<Exercise> exercises;

        if (bodyPartId != null) {
            exercises = exerciseRepository.findByBodyPartAndUser(userId, bodyPartId);
        } else {
            exercises = exerciseRepository.findAvailableExercises(userId);
        }

        return exercises.stream()
                .map(e -> new ExerciseResult(
                        e.getId(),
                        e.getName(),
                        e.getBodyPart().getId(),
                        e.getBodyPart().getName(),
                        e.getUser() != null // user가 있으면 커스텀 종목
                ))
                .toList();
    }

    /**
     * 전체 운동 부위 조회
     * 운동 기록 입력 폼에서 부위별 필터링 탭을 만들 때 사용
     */
    public List<BodyPartResult> getBodyParts() {
        return bodyPartRepository.findAll().stream()
                .map(bp -> new BodyPartResult(bp.getId(), bp.getName()))
                .toList();
    }

    /**
     * 커스텀 운동 종목 추가
     * 사용자가 기본 제공 목록에 없는 운동을 직접 추가하는 기능
     */
    @Transactional
    public ExerciseResult addCustomExercise(Long userId, String name, Long bodyPartId) {
        BodyPart bodyPart = bodyPartRepository.findById(bodyPartId)
                .orElseThrow(() -> BusinessException.notFound("운동 부위를 찾을 수 없습니다"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("사용자를 찾을 수 없습니다"));

        Exercise exercise = Exercise.builder()
                .name(name)
                .bodyPart(bodyPart)
                .user(user) // user 설정 → 커스텀 종목으로 저장
                .build();

        Exercise saved = exerciseRepository.save(exercise);

        return new ExerciseResult(
                saved.getId(), saved.getName(),
                bodyPart.getId(), bodyPart.getName(),
                true
        );
    }

    /**
     * 커스텀 운동 종목 삭제
     * 본인이 추가한 커스텀 종목만 삭제 가능
     */
    @Transactional
    public void deleteCustomExercise(Long userId, Long exerciseId) {
        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> BusinessException.notFound("운동 종목을 찾을 수 없습니다"));

        // 기본 종목은 삭제 불가 (user = null이면 기본 종목)
        if (exercise.getUser() == null) {
            throw BusinessException.badRequest("기본 제공 운동 종목은 삭제할 수 없습니다");
        }

        // 본인 것인지 확인
        if (!exercise.getUser().getId().equals(userId)) {
            throw BusinessException.badRequest("본인이 추가한 운동 종목만 삭제할 수 있습니다");
        }

        exerciseRepository.delete(exercise);
    }

    // 응답 레코드
    public record ExerciseResult(Long id, String name, Long bodyPartId,
                                  String bodyPartName, boolean isCustom) {}
    public record BodyPartResult(Long id, String name) {}
}
