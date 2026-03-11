package com.workout.app.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * [UserRepository - 사용자 데이터 접근]
 *
 * JpaRepository<User, Long>을 상속받으면:
 * - save(), findById(), findAll(), delete() 등 기본 CRUD 메서드 자동 제공
 * - 첫 번째 타입(User): 관리할 Entity
 * - 두 번째 타입(Long): PK 타입
 *
 * [메서드 이름으로 쿼리 자동 생성]
 * Spring Data JPA는 메서드 이름을 분석해서 자동으로 SQL을 만들어줍니다.
 * findByEmail → SELECT * FROM users WHERE email = ?
 * existsByEmail → SELECT COUNT(*) > 0 FROM users WHERE email = ?
 */
public interface UserRepository extends JpaRepository<User, Long> {

    // 이메일로 사용자 조회 (로그인 시 사용)
    // Optional: 결과가 없을 수도 있으므로 Optional로 감쌈 (NPE 방지)
    Optional<User> findByEmail(String email);

    // 이메일 중복 체크 (회원가입 시 사용)
    boolean existsByEmail(String email);

    // Refresh Token으로 사용자 조회 (토큰 갱신 시 사용)
    Optional<User> findByRefreshToken(String refreshToken);
}
