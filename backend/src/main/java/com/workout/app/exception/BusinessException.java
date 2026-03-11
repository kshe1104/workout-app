package com.workout.app.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * [BusinessException - 비즈니스 로직 예외]
 *
 * 애플리케이션의 비즈니스 규칙 위반 시 던지는 커스텀 예외입니다.
 *
 * 사용 예:
 * throw new BusinessException("이미 존재하는 이메일입니다", HttpStatus.CONFLICT);
 * throw new BusinessException("운동 기록을 찾을 수 없습니다", HttpStatus.NOT_FOUND);
 *
 * RuntimeException을 상속받아서 try-catch를 강제하지 않음 (unchecked exception)
 */
@Getter
public class BusinessException extends RuntimeException {

    // HTTP 상태 코드 (400 Bad Request, 404 Not Found, 409 Conflict 등)
    private final HttpStatus status;

    public BusinessException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    // 자주 쓰는 예외들을 정적 팩토리 메서드로 제공
    public static BusinessException notFound(String message) {
        return new BusinessException(message, HttpStatus.NOT_FOUND);
    }

    public static BusinessException badRequest(String message) {
        return new BusinessException(message, HttpStatus.BAD_REQUEST);
    }

    public static BusinessException conflict(String message) {
        return new BusinessException(message, HttpStatus.CONFLICT);
    }

    public static BusinessException unauthorized(String message) {
        return new BusinessException(message, HttpStatus.UNAUTHORIZED);
    }
}
