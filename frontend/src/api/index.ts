/**
 * [api/auth.ts - 인증 API 함수]
 *
 * API 함수를 별도 파일로 분리하는 이유:
 * - 컴포넌트에서 URL, 파라미터 구조를 몰라도 됨
 * - API 스펙이 바뀌면 이 파일만 수정하면 됨
 * - 재사용 편리
 */
import apiClient from './axios';
import type { LoginRequest, SignupRequest, AuthResponse } from '../types/workout';

export const authApi = {
  // 로그인 → Access Token + Refresh Token 반환
  login: (data: LoginRequest) =>
    apiClient.post<AuthResponse>('/api/auth/login', data),

  // 회원가입
  signup: (data: SignupRequest) =>
    apiClient.post('/api/auth/signup', data),

  // 로그아웃 (서버에서 Refresh Token 삭제)
  logout: () =>
    apiClient.delete('/api/auth/logout'),
};

/**
 * [api/workout.ts - 운동 기록 API 함수]
 */
import type {
  CalendarResponse, WorkoutDetail, WorkoutSaveRequest
} from '../types/workout';

export const workoutApi = {
  // 월별 달력 데이터 조회
  // params: { year: 2025, month: 3 }
  getCalendar: (year: number, month: number) =>
    apiClient.get<CalendarResponse>('/api/workouts/calendar', {
      params: { year, month }
    }),

  // 특정 날짜 운동 상세 조회
  // date: "2025-03-10"
  getDetail: (date: string) =>
    apiClient.get<WorkoutDetail>(`/api/workouts/${date}`),

  // 운동 기록 저장
  save: (data: WorkoutSaveRequest) =>
    apiClient.post('/api/workouts', data),

  // 운동 기록 수정
  update: (date: string, data: WorkoutSaveRequest) =>
    apiClient.put(`/api/workouts/${date}`, data),

  // 운동 기록 삭제
  delete: (date: string) =>
    apiClient.delete(`/api/workouts/${date}`),
};

/**
 * [api/exercise.ts - 운동 종목 API 함수]
 */
import type { Exercise, BodyPart } from '../types/workout';

export const exerciseApi = {
  // 운동 종목 목록 조회 (bodyPartId 없으면 전체)
  getExercises: (bodyPartId?: number) =>
    apiClient.get<Exercise[]>('/api/exercises', {
      params: bodyPartId ? { bodyPartId } : {}
    }),

  // 운동 부위 목록 조회
  getBodyParts: () =>
    apiClient.get<BodyPart[]>('/api/exercises/body-parts'),

  // 커스텀 종목 추가
  addCustom: (name: string, bodyPartId: number) =>
    apiClient.post<Exercise>('/api/exercises', { name, bodyPartId }),

  // 커스텀 종목 삭제
  deleteCustom: (exerciseId: number) =>
    apiClient.delete(`/api/exercises/${exerciseId}`),
};

// 챗봇 API 함수
export const chatApi = {
  sendMessage: (message: string) =>
    apiClient.post<{ reply: string }>('/api/chat', { message }),
};
