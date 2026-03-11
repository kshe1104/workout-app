package com.workout.app.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;

/**
 * [JwtTokenProvider - JWT 토큰 관리]
 *
 * JWT(JSON Web Token)란?
 * 서버가 사용자 인증 정보를 암호화해서 클라이언트에게 주는 "디지털 신분증"입니다.
 *
 * 구조: Header.Payload.Signature
 * - Header: 알고리즘 정보 (HS256 등)
 * - Payload: 실제 데이터 (userId, email, role, 만료시간)
 * - Signature: 위변조 방지 서명
 *
 * [토큰 종류]
 * Access Token (15분): API 요청 시 매번 헤더에 첨부
 * Refresh Token (7일): Access Token 만료 시 재발급 요청에만 사용
 *
 * [왜 두 가지 토큰을 쓰나?]
 * Access Token만 쓰면: 만료가 짧아서 자주 로그아웃됨 (UX 나쁨)
 *                      만료가 길면: 토큰 탈취 시 오래 악용 가능 (보안 위험)
 * → 짧은 Access + 긴 Refresh 조합으로 보안과 UX 균형
 */
@Slf4j  // log.info(), log.error() 등 로깅 메서드 자동 생성
@Component  // Spring 빈으로 등록 → 다른 클래스에서 @Autowired로 주입 가능
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    // @Value: application.yml의 값을 주입받음
    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration}") long accessTokenExpiration,
            @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration) {

        // 문자열 비밀키를 암호화에 사용 가능한 SecretKey 객체로 변환
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    /**
     * Access Token 생성
     * @param userId 사용자 ID (토큰에 담을 식별자)
     * @param email  사용자 이메일
     * @param role   사용자 권한 (ROLE_USER 등)
     */
    public String generateAccessToken(Long userId, String email, String role) {
        return generateToken(userId, email, role, accessTokenExpiration);
    }

    /**
     * Refresh Token 생성
     * Refresh Token에는 최소한의 정보만 담음 (userId만)
     */
    public String generateRefreshToken(Long userId) {
        return Jwts.builder()
                .subject(String.valueOf(userId))  // 토큰 주체(subject): userId
                .issuedAt(new Date())              // 발급 시간
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .signWith(secretKey)               // 서명 (위변조 방지)
                .compact();
    }

    private String generateToken(Long userId, String email, String role, long expiration) {
        return Jwts.builder()
                .subject(String.valueOf(userId))   // subject: 토큰 주체
                .claim("email", email)             // claim: 토큰에 담을 추가 정보
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(secretKey)
                .compact();
    }

    /**
     * 토큰에서 Authentication 객체 추출
     * Spring Security가 "현재 로그인한 사용자가 누구인가" 파악할 때 사용
     */
    public Authentication getAuthentication(String token) {
        Claims claims = getClaims(token);

        Long userId = Long.parseLong(claims.getSubject());
        String role = claims.get("role", String.class);
        String email = claims.get("email", String.class);

        // UserDetails 대신 간단하게 CustomUserPrincipal 사용
        CustomUserPrincipal principal = new CustomUserPrincipal(userId, email);

        // UsernamePasswordAuthenticationToken: Spring Security의 인증 객체
        // 파라미터: (사용자 정보, 자격증명, 권한목록)
        return new UsernamePasswordAuthenticationToken(
                principal,
                null, // 인증 완료 후에는 자격증명(비밀번호) 필요 없음
                Collections.singleton(new SimpleGrantedAuthority(role))
        );
    }

    /**
     * 토큰에서 userId 추출 (Refresh Token 검증 시 사용)
     */
    public Long getUserId(String token) {
        return Long.parseLong(getClaims(token).getSubject());
    }

    /**
     * 토큰 유효성 검증
     * 만료, 서명 오류, 형식 오류 등을 체크
     */
    public boolean validateToken(String token) {
        try {
            getClaims(token); // 파싱 시 자동으로 만료/서명 검증
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("만료된 JWT 토큰: {}", e.getMessage());
        } catch (JwtException e) {
            log.warn("유효하지 않은 JWT 토큰: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT 토큰이 비어있음: {}", e.getMessage());
        }
        return false;
    }

    // 토큰 파싱 → Claims(payload 데이터) 추출
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)  // 서명 검증
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
