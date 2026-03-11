package com.workout.app.domain.exercise;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * [BodyPartRepository]
 *
 * 기본 CRUD만 필요하므로 JpaRepository만 상속합니다.
 * findAll()로 전체 부위 목록을 가져올 때 사용합니다.
 */
public interface BodyPartRepository extends JpaRepository<BodyPart, Long> {
}
