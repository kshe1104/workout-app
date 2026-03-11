package com.workout.app.domain.user;

import com.workout.app.exception.BusinessException;
import com.workout.app.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * [AuthService - 인증 서비스]
 *
 * [@Service]: Spring 빈으로 등록. 비즈니스 로직을 담는 계층
 *
 * [@Transactional]이란?
 * DB 작업 여러 개를 하나의 단위로 묶습니다.
 * 중간에 오류 발생 시 모두 롤백(취소)됩니다.
 *
 * 예: 회원가입 시 User 저장 + Refresh Token 저장을 하나의 트랜잭션으로 처리
 * → User 저장은 성공했는데 Refresh Token 저장 실패 시 User 저장도 취소됨
 *
 * readOnly = true: 읽기 전용 트랜잭션 (조회 쿼리 성능 최적화)
 * → 불필요한 dirty checking(변경 감지)을 하지 않음
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;  // SecurityConfig에서 빈 등록한 BCryptPasswordEncoder
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 회원가입
     */
    @Transactional  // 쓰기 작업이므로 readOnly = false (기본값)
    public void signup(String email, String password, String nickname) {
        // 이메일 중복 체크
        if (userRepository.existsByEmail(email)) {
            // HTTP 409 Conflict로 응답
            throw BusinessException.conflict("이미 사용 중인 이메일입니다");
        }

        // 비밀번호 암호화
        // passwordEncoder.encode(): 평문 → BCrypt 해시값
        // 같은 비밀번호도 호출할 때마다 다른 해시값 생성 (salt 때문에)
        String encodedPassword = passwordEncoder.encode(password);

        User user = User.builder()
                .email(email)
                .password(encodedPassword)
                .nickname(nickname)
                .role(Role.ROLE_USER)
                .build();

        userRepository.save(user);
    }

    /**
     * 로그인
     * @return [accessToken, refreshToken]
     */
    @Transactional
    public TokenResult login(String email, String password) {
        // 1. 이메일로 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> BusinessException.unauthorized("이메일 또는 비밀번호가 올바르지 않습니다"));

        // 2. 비밀번호 검증
        // passwordEncoder.matches(평문, 해시값): 비밀번호 일치 여부 확인
        if (!passwordEncoder.matches(password, user.getPassword())) {
            // 보안상 "비밀번호가 틀렸습니다"가 아닌 모호한 메시지 사용
            // (어떤 값이 틀렸는지 공격자에게 알려주지 않기 위해)
            throw BusinessException.unauthorized("이메일 또는 비밀번호가 올바르지 않습니다");
        }

        // 3. JWT 토큰 생성
        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getId(), user.getEmail(), user.getRole().name());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        // 4. Refresh Token DB 저장
        // (재발급 요청 시 DB의 값과 비교해서 유효성 검증)
        user.updateRefreshToken(refreshToken);

        return new TokenResult(accessToken, refreshToken, user.getEmail(), user.getNickname());
    }

    /**
     * Access Token 재발급
     * Refresh Token이 유효하면 새 Access Token 발급
     */
    @Transactional
    public String refreshAccessToken(String refreshToken) {
        // 1. Refresh Token 유효성 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw BusinessException.unauthorized("유효하지 않은 Refresh Token입니다");
        }

        // 2. DB에서 Refresh Token으로 사용자 조회
        // (탈취된 토큰으로 재발급 요청하는 것을 방지)
        User user = userRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> BusinessException.unauthorized("Refresh Token이 일치하지 않습니다"));

        // 3. 새 Access Token 발급
        return jwtTokenProvider.generateAccessToken(
                user.getId(), user.getEmail(), user.getRole().name());
    }

    /**
     * 로그아웃
     * DB에서 Refresh Token 삭제 → 재발급 불가 상태로 만들기
     */
    @Transactional
    public void logout(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("사용자를 찾을 수 없습니다"));
        user.clearRefreshToken();
        // @Transactional이 있으므로 메서드 종료 시 자동으로 DB에 반영 (dirty checking)
    }

    // 토큰 반환용 내부 레코드 (간단한 DTO)
    public record TokenResult(String accessToken, String refreshToken,
                              String email, String nickname) {}
}
