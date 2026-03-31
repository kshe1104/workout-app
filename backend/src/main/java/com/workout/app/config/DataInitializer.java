package com.workout.app.config;

import com.workout.app.domain.exercise.BodyPart;
import com.workout.app.domain.exercise.BodyPartRepository;
import com.workout.app.domain.exercise.Exercise;
import com.workout.app.domain.exercise.ExerciseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * [DataInitializer - 초기 데이터 로딩]
 *
 * ApplicationRunner: Spring이 완전히 뜬 후 실행됨
 * → JPA가 테이블을 먼저 만들고 나서 데이터를 넣으므로 타이밍 문제 없음
 * → data.sql 대신 이 방식 사용
 *
 * 이미 데이터가 있으면 스킵하므로 재시작해도 중복 삽입 안 됨
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final BodyPartRepository bodyPartRepository;
    private final ExerciseRepository exerciseRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (bodyPartRepository.count() > 0) {
            log.info("초기 데이터가 이미 존재합니다. 스킵합니다.");
            return;
        }

        log.info("초기 데이터 로딩 시작...");

        // 운동 부위 삽입
        BodyPart 가슴   = bodyPartRepository.save(new BodyPart("가슴"));
        BodyPart 등     = bodyPartRepository.save(new BodyPart("등"));
        BodyPart 어깨   = bodyPartRepository.save(new BodyPart("어깨"));
        BodyPart 이두   = bodyPartRepository.save(new BodyPart("이두"));
        BodyPart 삼두   = bodyPartRepository.save(new BodyPart("삼두"));
        BodyPart 하체   = bodyPartRepository.save(new BodyPart("하체"));
        BodyPart 복근   = bodyPartRepository.save(new BodyPart("복근"));
        BodyPart 유산소 = bodyPartRepository.save(new BodyPart("유산소"));

        // 운동 종목 삽입
        exerciseRepository.saveAll(List.of(
                // 가슴
                new Exercise("벤치프레스",               가슴,   null),
                new Exercise("인클라인 벤치프레스",       가슴,   null),
                new Exercise("덤벨 플라이",               가슴,   null),
                new Exercise("푸시업",                    가슴,   null),
                new Exercise("케이블 크로스오버",         가슴,   null),
                // 등
                new Exercise("데드리프트",                등,     null),
                new Exercise("풀업",                      등,     null),
                new Exercise("바벨 로우",                 등,     null),
                new Exercise("렛 풀다운",                 등,     null),
                new Exercise("시티드 로우",               등,     null),
                // 어깨
                new Exercise("오버헤드 프레스",           어깨,   null),
                new Exercise("사이드 레터럴 레이즈",      어깨,   null),
                new Exercise("프론트 레이즈",             어깨,   null),
                new Exercise("페이스 풀",                 어깨,   null),
                // 이두
                new Exercise("바벨 컬",                   이두,   null),
                new Exercise("덤벨 컬",                   이두,   null),
                new Exercise("해머 컬",                   이두,   null),
                // 삼두
                new Exercise("트라이셉스 딥",             삼두,   null),
                new Exercise("케이블 푸시다운",           삼두,   null),
                new Exercise("오버헤드 트라이셉스 익스텐션", 삼두, null),
                // 하체
                new Exercise("스쿼트",                    하체,   null),
                new Exercise("레그 프레스",               하체,   null),
                new Exercise("런지",                      하체,   null),
                new Exercise("레그 컬",                   하체,   null),
                new Exercise("레그 익스텐션",             하체,   null),
                new Exercise("카프 레이즈",               하체,   null),
                // 복근
                new Exercise("크런치",                    복근,   null),
                new Exercise("플랭크",                    복근,   null),
                new Exercise("레그 레이즈",               복근,   null),
                // 유산소
                new Exercise("러닝",                      유산소, null),
                new Exercise("사이클",                    유산소, null),
                new Exercise("로잉머신",                  유산소, null),
                new Exercise("줄넘기",                    유산소, null)
        ));

        log.info("초기 데이터 로딩 완료! 부위 8개, 종목 33개 삽입됨");
    }
}