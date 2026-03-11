package com.workout.app.config;

import com.workout.app.security.JwtAuthenticationFilter;
import com.workout.app.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * [SecurityConfig - Spring Security 핵심 설정]
 *
 * Spring Security는 기본적으로 모든 요청에 인증을 요구합니다.
 * 이 설정 파일에서 "어떤 요청은 허용하고 어떤 요청은 막을지" 규칙을 정합니다.
 *
 * [@Configuration]: 이 클래스가 설정 클래스임을 Spring에 알림
 * [@EnableWebSecurity]: Spring Security 활성화
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * [SecurityFilterChain - 보안 필터 체인]
     *
     * HTTP 요청에 대한 보안 규칙을 정의합니다.
     * 이 빈이 Spring Security의 핵심 설정입니다.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // ── CSRF 비활성화 ─────────────────────────────────────────
            // CSRF(Cross-Site Request Forgery): 다른 사이트에서 우리 서버로 몰래 요청 보내는 공격
            // REST API + JWT 방식에서는 CSRF 토큰이 필요 없음
            // (세션이 없으므로 CSRF 공격 자체가 성립하지 않음)
            .csrf(AbstractHttpConfigurer::disable)

            // ── CORS 설정 ─────────────────────────────────────────────
            // CORS: 다른 출처(도메인/포트)에서 오는 요청을 허용할지 결정
            // React(3000포트)에서 Spring(8080포트)으로 요청 시 CORS 정책에 걸림
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // ── 세션 비활성화 ─────────────────────────────────────────
            // JWT를 사용하므로 서버 세션이 필요 없음
            // STATELESS: 서버가 상태(세션)를 유지하지 않음 → 확장성 좋음
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // ── URL별 접근 권한 설정 ──────────────────────────────────
            .authorizeHttpRequests(auth -> auth
                // 로그인, 회원가입은 인증 없이 접근 가능
                .requestMatchers("/api/auth/**").permitAll()
                // H2 콘솔 (개발 환경에서만 사용, 운영에서는 제거)
                .requestMatchers("/h2-console/**").permitAll()
                // OPTIONS 요청 허용 (브라우저가 CORS preflight 요청을 보낼 때)
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // 그 외 모든 요청은 인증 필요
                .anyRequest().authenticated()
            )

            // ── JWT 필터 등록 ─────────────────────────────────────────
            // UsernamePasswordAuthenticationFilter 앞에 JWT 필터 삽입
            // → 모든 요청에서 JWT 토큰을 먼저 검증
            .addFilterBefore(
                new JwtAuthenticationFilter(jwtTokenProvider),
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }

    /**
     * [PasswordEncoder - 비밀번호 암호화]
     *
     * BCrypt: 비밀번호 단방향 해시 알고리즘
     * - 같은 비밀번호도 매번 다른 해시값 생성 (salt 자동 처리)
     * - 역방향 복호화 불가능
     * - 로그인 시: 입력한 비밀번호를 암호화해서 DB의 해시값과 비교
     *
     * 절대 비밀번호를 평문으로 DB에 저장하면 안 됩니다!
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * [AuthenticationManager - 인증 매니저]
     *
     * Spring Security의 인증 처리를 담당합니다.
     * UserDetailsService를 통해 사용자를 조회하고 비밀번호를 검증합니다.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * [CORS 설정]
     *
     * CORS(Cross-Origin Resource Sharing):
     * 브라우저는 보안상 다른 출처의 요청을 기본적으로 차단합니다.
     * React(localhost:3000)에서 Spring(localhost:8080)으로 요청하려면
     * 서버에서 "이 출처는 허용한다"고 명시해야 합니다.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // 허용할 출처 (개발 환경)
        config.setAllowedOrigins(List.of("http://localhost:3000"));

        // 허용할 HTTP 메서드
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // 허용할 헤더 (Authorization에 JWT 토큰을 담아서 보내므로 필수)
        config.setAllowedHeaders(List.of("*"));

        // 쿠키/인증 정보 포함 허용
        config.setAllowCredentials(true);

        // preflight 캐시 시간 (브라우저가 OPTIONS 요청을 매번 보내지 않도록)
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config); // 모든 경로에 적용
        return source;
    }
}
