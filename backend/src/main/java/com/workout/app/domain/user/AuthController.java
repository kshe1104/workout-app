package com.workout.app.domain.user;

import com.workout.app.security.CustomUserPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * [AuthController - 인증 API]
 *
 * [@RestController]: @Controller + @ResponseBody
 * → 모든 메서드의 반환값이 자동으로 JSON으로 변환됨
 *
 * [@RequestMapping]: 이 Controller의 기본 URL 경로
 *
 * [@RequiredArgsConstructor]: final 필드를 생성자로 주입 (의존성 주입)
 * Spring이 AuthService 빈을 찾아서 자동으로 주입해줌
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 회원가입
     * POST /api/auth/signup
     *
     * @Valid: 요청 바디의 검증 어노테이션(@NotBlank, @Email 등) 실행
     * @RequestBody: HTTP 요청 바디의 JSON을 자바 객체로 변환
     */
    @PostMapping("/signup")
    public ResponseEntity<Map<String, String>> signup(@Valid @RequestBody SignupRequest request) {
        authService.signup(request.getEmail(), request.getPassword(), request.getNickname());
        // 201 Created: 리소스 생성 성공
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "회원가입이 완료되었습니다"));
    }

    /**
     * 로그인
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthService.TokenResult result = authService.login(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(new LoginResponse(
                result.accessToken(),
                result.refreshToken(),
                result.email(),
                result.nickname()
        ));
    }

    /**
     * Access Token 재발급
     * POST /api/auth/refresh
     */
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refresh(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        String newAccessToken = authService.refreshAccessToken(refreshToken);
        return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
    }

    /**
     * 로그아웃
     * DELETE /api/auth/logout
     *
     * @AuthenticationPrincipal: SecurityContext에 저장된 현재 로그인 사용자 정보 주입
     * JwtAuthenticationFilter에서 저장한 CustomUserPrincipal 객체를 가져옴
     */
    @DeleteMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal CustomUserPrincipal principal) {
        authService.logout(principal.getId());
        // 204 No Content: 성공했지만 반환할 데이터 없음
        return ResponseEntity.noContent().build();
    }

    // ── 요청/응답 DTO (이 Controller에서만 사용하므로 내부 클래스로) ────

    @Getter
    static class SignupRequest {
        @NotBlank @Email
        private String email;
        @NotBlank @Size(min = 8)
        private String password;
        @NotBlank @Size(min = 2, max = 20)
        private String nickname;
    }

    @Getter
    static class LoginRequest {
        @NotBlank @Email
        private String email;
        @NotBlank
        private String password;
    }

    record LoginResponse(String accessToken, String refreshToken,
                         String email, String nickname) {}
}
