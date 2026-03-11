package com.workout.app.domain.exercise;

import jakarta.persistence.*;
import lombok.*;

/**
 * [BodyPart - 운동 부위]
 *
 * 미리 정의된 운동 부위 카테고리입니다.
 * 예: 가슴, 등, 어깨, 이두, 삼두, 하체, 복근, 유산소
 *
 * 앱 시작 시 data.sql로 기본 데이터를 넣어둡니다.
 */
@Entity
@Table(name = "body_parts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BodyPart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name; // 가슴, 등, 어깨, 이두, 삼두, 하체, 복근, 유산소

    @Builder
    public BodyPart(String name) {
        this.name = name;
    }
}
