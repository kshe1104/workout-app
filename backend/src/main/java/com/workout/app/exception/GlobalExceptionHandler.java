package com.workout.app.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * [GlobalExceptionHandler - 전역 예외 처리]
 *
 * @RestControllerAdvice: 모든 Controller에서 발생하는 예외를 이 클래스에서 일괄 처리합니다.
 *
 * 왜 필요한가?
 * 예외를 각 Controller마다 try-catch로 처리하면:
 * - 코드 중복이 많아짐
 * - 응답 형식이 일관되지 않음
 *
 * → 한 곳에서 모든 예외를 잡아 일관된 형태의 에러 응답을 만들어줍니다.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 커스텀 비즈니스 예외 처리
     * 예: 이미 존재하는 이메일, 잘못된 비밀번호 등
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        log.warn("비즈니스 예외 발생: {}", e.getMessage());
        return ResponseEntity
                .status(e.getStatus())
                .body(new ErrorResponse(e.getMessage()));
    }

    /**
     * @Valid 검증 실패 처리
     * 예: 이메일 형식 오류, 비밀번호 길이 부족 등
     *
     * 응답 예시:
     * {
     *   "message": "입력값이 올바르지 않습니다",
     *   "errors": {
     *     "email": "올바른 이메일 형식이 아닙니다",
     *     "password": "비밀번호는 8자 이상이어야 합니다"
     *   }
     * }
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationException(
            MethodArgumentNotValidException e) {

        Map<String, String> errors = new HashMap<>();
        // 검증 실패한 각 필드와 메시지를 Map으로 수집
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            errors.put(fieldName, message);
        });

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ValidationErrorResponse("입력값이 올바르지 않습니다", errors));
    }

    /**
     * 예상치 못한 서버 에러 처리
     * 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("예상치 못한 오류 발생", e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("서버 내부 오류가 발생했습니다"));
    }

    // 에러 응답 DTO (내부 클래스로 간단히 정의)
    record ErrorResponse(String message) {}
    record ValidationErrorResponse(String message, Map<String, String> errors) {}
}
