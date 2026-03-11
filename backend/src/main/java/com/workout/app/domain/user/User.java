package com.workout.app.domain.user;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * [Entity란?]
 * @Entity 어노테이션이 붙은 클래스는 DB 테이블과 1:1로 매핑됩니다.
 * 이 클래스의 필드 하나하나가 테이블의 컬럼이 됩니다.
 *
 * [users 테이블과 매핑]
 * id, email, password, nickname, role, created_at, updated_at 컬럼 생성
 */
@Entity
@Table(
    name = "users",
    // 이메일은 중복 불가 → DB 레벨에서 unique 제약조건 추가
    uniqueConstraints = @UniqueConstraint(columnNames = "email")
)
@Getter
// @NoArgsConstructor(access = PROTECTED): JPA는 기본 생성자가 필요한데,
// PROTECTED로 설정해서 외부에서 new User() 직접 생성을 막음
// → Builder 패턴 강제로 안전한 객체 생성 유도
@NoArgsConstructor(access = AccessLevel.PROTECTED)
// @EntityListeners: createdAt, updatedAt 자동 처리를 위한 Auditing 리스너 등록
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // IDENTITY: MySQL의 AUTO_INCREMENT 사용 (DB가 ID 자동 증가)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    // password는 절대 평문 저장 금지 → BCrypt로 암호화된 값이 저장됨
    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 50)
    private String nickname;

    // 사용자 권한 (ROLE_USER, ROLE_ADMIN)
    // @Enumerated(EnumType.STRING): enum을 숫자가 아닌 문자열로 DB에 저장
    // → DB에서 직접 봤을 때 의미 파악 가능
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // Refresh Token: 로그인 유지를 위해 DB에 저장
    // Access Token이 만료됐을 때 이 값으로 새 토큰 발급
    @Column(length = 512)
    private String refreshToken;

    // @CreatedDate: 엔티티가 처음 저장될 때 자동으로 현재 시간 입력
    @CreatedDate
    @Column(updatable = false) // 생성 후 수정 불가
    private LocalDateTime createdAt;

    // @LastModifiedDate: 엔티티가 수정될 때마다 자동으로 현재 시간 갱신
    @LastModifiedDate
    private LocalDateTime updatedAt;

    // @Builder: User.builder().email("...").password("...").build() 형태로 생성
    // 파라미터 순서 실수 방지, 어떤 값을 설정하는지 명확히 표현 가능
    @Builder
    public User(String email, String password, String nickname, Role role) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.role = role;
    }

    // Refresh Token 업데이트 메서드
    // 외부에서 직접 필드를 수정하는 대신 메서드를 통해 수정 (캡슐화)
    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    // 로그아웃 시 Refresh Token 무효화
    public void clearRefreshToken() {
        this.refreshToken = null;
    }
}
