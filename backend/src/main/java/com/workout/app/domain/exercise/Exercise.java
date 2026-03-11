package com.workout.app.domain.exercise;

import com.workout.app.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

/**
 * [Exercise - 운동 종목]
 *
 * 두 종류의 운동 종목이 있습니다:
 * 1. 기본 종목 (user = null): 앱에서 기본 제공 (벤치프레스, 스쿼트 등)
 * 2. 커스텀 종목 (user != null): 사용자가 직접 추가한 종목
 *
 * [조회 시 필터링]
 * 사용자 A가 종목 목록 조회 시:
 * → user = null (기본 종목) OR user = A (A의 커스텀 종목) 모두 조회
 */
@Entity
@Table(name = "exercises")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Exercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name; // 벤치프레스, 스쿼트, 데드리프트 등

    // 어느 부위 운동인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "body_part_id", nullable = false)
    private BodyPart bodyPart;

    // null이면 기본 종목, 값이 있으면 해당 사용자의 커스텀 종목
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Builder
    public Exercise(String name, BodyPart bodyPart, User user) {
        this.name = name;
        this.bodyPart = bodyPart;
        this.user = user; // 기본 종목이면 null 전달
    }
}
