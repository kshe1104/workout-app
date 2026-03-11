/**
 * [hooks/useCalendar.ts - 달력 데이터 훅]
 *
 * [React Query(TanStack Query)란?]
 * 서버에서 가져오는 데이터(API 응답)를 관리하는 라이브러리입니다.
 *
 * 일반 useState + useEffect로 API 호출하면:
 * - 로딩 상태, 에러 상태를 직접 관리해야 함
 * - 같은 데이터를 여러 컴포넌트에서 요청하면 중복 API 호출
 * - 페이지 이동 후 돌아오면 다시 fetch
 *
 * React Query 사용하면:
 * - isLoading, error 자동 제공
 * - 같은 queryKey면 캐싱 → 중복 요청 없음
 * - 월 이동 시 이전에 봤던 달은 캐시에서 즉시 표시
 *
 * [useQuery 기본 구조]
 * const { data, isLoading, error } = useQuery({
 *   queryKey: ['고유한 키'],   // 이 키로 캐시를 구분함
 *   queryFn: () => API 호출,  // 실제 데이터를 가져오는 함수
 * });
 */
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { workoutApi, exerciseApi } from '../api';

// 달력 데이터 조회 훅
export function useCalendar(year: number, month: number) {
  return useQuery({
    // queryKey: 캐시 키. year/month가 바뀌면 새로 fetch
    queryKey: ['calendar', year, month],
    queryFn: async () => {
      const response = await workoutApi.getCalendar(year, month);
      return response.data;
    },
    // staleTime: 이 시간(5분) 동안은 캐시를 신선하다고 판단 → 재요청 안 함
    staleTime: 5 * 60 * 1000,
  });
}

// 특정 날짜 운동 상세 조회 훅
export function useWorkoutDetail(date: string | null) {
  return useQuery({
    queryKey: ['workout', date],
    queryFn: async () => {
      const response = await workoutApi.getDetail(date!);
      return response.data;
    },
    // enabled: date가 null이면 쿼리 실행 안 함
    // (날짜를 클릭하지 않은 상태에서는 조회하지 않음)
    enabled: !!date,
    retry: false, // 404(기록 없는 날) 에러 시 재시도 안 함
  });
}

// 운동 기록 저장 훅
export function useSaveWorkout() {
  // useQueryClient: 캐시를 직접 조작하기 위한 인스턴스
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: workoutApi.save,
    onSuccess: (_, variables) => {
      // 저장 성공 후 달력 캐시 무효화 → 자동으로 달력 재조회
      const date = new Date(variables.date);
      queryClient.invalidateQueries({
        queryKey: ['calendar', date.getFullYear(), date.getMonth() + 1]
      });
      // 해당 날짜의 상세 캐시도 무효화
      queryClient.invalidateQueries({ queryKey: ['workout', variables.date] });
    },
  });
}

// 운동 기록 수정 훅
export function useUpdateWorkout(date: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: Parameters<typeof workoutApi.update>[1]) =>
      workoutApi.update(date, data),
    onSuccess: () => {
      const d = new Date(date);
      queryClient.invalidateQueries({ queryKey: ['calendar', d.getFullYear(), d.getMonth() + 1] });
      queryClient.invalidateQueries({ queryKey: ['workout', date] });
    },
  });
}

// 운동 기록 삭제 훅
export function useDeleteWorkout() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: workoutApi.delete,
    onSuccess: (_, date) => {
      const d = new Date(date);
      queryClient.invalidateQueries({ queryKey: ['calendar', d.getFullYear(), d.getMonth() + 1] });
      queryClient.invalidateQueries({ queryKey: ['workout', date] });
    },
  });
}

// 운동 부위 목록 조회 훅
export function useBodyParts() {
  return useQuery({
    queryKey: ['bodyParts'],
    queryFn: async () => {
      const response = await exerciseApi.getBodyParts();
      return response.data;
    },
    staleTime: Infinity, // 운동 부위는 거의 안 바뀌므로 영구 캐시
  });
}

// 운동 종목 목록 조회 훅
export function useExercises(bodyPartId?: number) {
  return useQuery({
    queryKey: ['exercises', bodyPartId],
    queryFn: async () => {
      const response = await exerciseApi.getExercises(bodyPartId);
      return response.data;
    },
    staleTime: 10 * 60 * 1000,
  });
}

// 운동 종목 추가 훅
export function useAddExercise() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ name, bodyPartId }: { name: string; bodyPartId: number }) =>
      exerciseApi.addCustom(name, bodyPartId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['exercises'] });
    },
  });
}

// 운동 종목 삭제 훅
export function useDeleteExercise() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (exerciseId: number) => exerciseApi.deleteCustom(exerciseId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['exercises'] });
    },
  });
}
