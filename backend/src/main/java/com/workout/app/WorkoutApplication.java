package com.workout.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * [WorkoutApplication - Spring Boot 진입점]
 *
 * @SpringBootApplication = @Configuration + @EnableAutoConfiguration + @ComponentScan
 * - @Configuration: 이 클래스가 Spring 설정 클래스임을 표시
 * - @EnableAutoConfiguration: 클래스패스 기반으로 빈 자동 설정
 *   (spring-boot-starter-web이 있으면 DispatcherServlet 자동 설정 등)
 * - @ComponentScan: 이 패키지 하위의 @Component, @Service, @Repository, @Controller 스캔
 */
@SpringBootApplication
public class WorkoutApplication {
    public static void main(String[] args) {
        SpringApplication.run(WorkoutApplication.class, args);
    }
}
