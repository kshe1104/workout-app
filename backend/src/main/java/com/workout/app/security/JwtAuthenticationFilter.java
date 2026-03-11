package com.workout.app.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * [JwtAuthenticationFilter - JWT 인증 필터]
 *
 * 모든 HTTP 요청이 Controller에 도달하기 전에 이 필터를 통과합니다.
 * 요청 헤더의 JWT 토큰을 검증하고, 유효하면 SecurityContext에 인증 정보를 저장합니다.
 *
 * [필터 동작 흐름]
 * 클라이언트 요청
 *   → JwtAuthenticationFilter (토큰 검증)
 *   → SecurityContext에 Authentication 저장
 *   → Controller 실행
 *   → @AuthenticationPrincipal로 사용자 정보 접근
 *
 * [OncePerRequestFilter]
 * 요청당 딱 한 번만 실행되는 필터입니다.
 * (서블릿 필터는 포워딩 시 재실행될 수 있는데, 이를 방지)
 */
@Slf4j
@RequiredArgsConstructor // final 필드를 파라미터로 받는 생성자 자동 생성 (의존성 주입)
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 1. 요청 헤더에서 JWT 토큰 추출
        String token = resolveToken(request);

        // 2. 토큰이 있고 유효하면 SecurityContext에 인증 정보 저장
        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
            // 토큰에서 Authentication 객체 생성
            Authentication authentication = jwtTokenProvider.getAuthentication(token);

            // SecurityContext: 현재 요청의 인증 정보를 저장하는 공간
            // 여기에 저장하면 어디서든 SecurityContextHolder.getContext().getAuthentication()으로 접근 가능
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("SecurityContext에 '{}' 인증 정보 저장", authentication.getName());
        }

        // 3. 다음 필터로 요청 전달 (필터 체인 계속 진행)
        filterChain.doFilter(request, response);
    }

    /**
     * HTTP 요청 헤더에서 Bearer 토큰 추출
     *
     * 클라이언트는 토큰을 이렇게 보냅니다:
     * Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIn0...
     *
     * "Bearer " 접두사를 제거하고 순수 토큰 문자열만 반환합니다.
     */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        // "Bearer "로 시작하는 경우에만 토큰 추출
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // "Bearer " (7글자) 이후 문자열
        }
        return null;
    }
}
