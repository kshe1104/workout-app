/**
 * [types/workout.ts - TypeScript 타입 정의]
 *
 * TypeScript를 쓰는 이유:
 * - API 응답의 구조를 미리 정의해두면 오탈자, 잘못된 필드 접근을 컴파일 단계에서 잡아줌
 * - IDE 자동완성으로 개발 속도 향상
 * - 백엔드 API 스펙이 바뀌면 어디를 고쳐야 하는지 바로 파악 가능
 */

// ── 인증 관련 타입 ─────────────────────────────────────────────────

export interface LoginRequest {
  email: string;
  password: string;
}

export interface SignupRequest {
  email: string;
  password: string;
  nickname: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  email: string;
  nickname: string;
}

// 현재 로그인한 사용자 상태 (Zustand store에서 관리)
export interface AuthUser {
  email: string;
  nickname: string;
}

// ── 달력 관련 타입 ─────────────────────────────────────────────────

export interface CalendarDay {
  date: string;        // "2025-03-10" 형식
  hasWorkout: boolean;
  bodyParts: string[]; // ["가슴", "삼두"] → 달력 셀에 색상으로 표시
}

export interface CalendarResponse {
  year: number;
  month: number;
  workoutDays: CalendarDay[];
}

// ── 운동 기록 상세 타입 ────────────────────────────────────────────

export interface SetDetail {
  id: number;
  setNumber: number;
  weightKg: number | null; // null이면 맨몸운동
  reps: number;
  completed: boolean;
}

export interface ExerciseDetail {
  id: number;
  name: string;
  bodyPart: string;
  orderIndex: number;
  sets: SetDetail[];
}

export interface WorkoutDetail {
  date: string;
  memo: string | null;
  exercises: ExerciseDetail[];
}

// ── 운동 종목 타입 ─────────────────────────────────────────────────

export interface Exercise {
  id: number;
  name: string;
  bodyPartId: number;
  bodyPartName: string;
  isCustom: boolean;
}

export interface BodyPart {
  id: number;
  name: string;
}

// ── 운동 기록 저장 요청 타입 ──────────────────────────────────────

export interface SetSaveRequest {
  setNumber: number;
  weightKg: number | null;
  reps: number;
  completed: boolean;
}

export interface ExerciseSaveRequest {
  exerciseId: number;
  orderIndex: number;
  sets: SetSaveRequest[];
}

export interface WorkoutSaveRequest {
  date: string;
  memo: string;
  exercises: ExerciseSaveRequest[];
}
