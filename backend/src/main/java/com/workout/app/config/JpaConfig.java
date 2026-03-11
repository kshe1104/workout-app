package com.workout.app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * [JpaConfig - JPA 설정]
 *
 * @EnableJpaAuditing: Entity의 @CreatedDate, @LastModifiedDate 자동 처리 활성화
 *
 * 이 어노테이션이 있어야 User, WorkoutLog 등의 createdAt, updatedAt이
 * 자동으로 채워집니다.
 *
 * SecurityConfig와 분리한 이유:
 * @SpringBootApplication에 @EnableJpaAuditing을 붙이면
 * Security 테스트 시 JPA 관련 빈이 없어서 오류가 발생할 수 있음
 * → 별도 @Configuration 클래스로 분리하는 것이 안전
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
