/**
 * [components/WorkoutDetail/WorkoutDetailPanel.tsx - 운동 상세 패널]
 *
 * 날짜를 클릭하면 오른쪽에 표시되는 상세 기록 패널입니다.
 *
 * 상태별 표시:
 * - 날짜 미선택: 안내 메시지
 * - 로딩 중: 스피너
 * - 기록 없음 (404): "기록 없음 + 기록하기 버튼"
 * - 기록 있음: 운동 종목별 세트 정보
 */
import { useWorkoutDetail, useDeleteWorkout } from '../../hooks/useWorkout';
import styles from './WorkoutDetailPanel.module.css';
import dayjs from 'dayjs';

interface Props {
  selectedDate: string | null;
  onEdit: () => void;   // 수정 버튼 클릭 시 폼 열기
  onAdd: () => void;    // 기록하기 버튼 클릭 시 폼 열기
}

export default function WorkoutDetailPanel({ selectedDate, onEdit, onAdd }: Props) {
  const { data, isLoading, error } = useWorkoutDetail(selectedDate);
  const deleteWorkout = useDeleteWorkout();

  // 날짜 미선택 상태
  if (!selectedDate) {
    return (
      <div className={styles.panel}>
        <div className={styles.empty}>
          <span className={styles.emptyIcon}>📅</span>
          <p>날짜를 선택하면<br />운동 기록을 확인할 수 있어요</p>
        </div>
      </div>
    );
  }

  // 로딩 상태
  if (isLoading) {
    return (
      <div className={styles.panel}>
        <div className={styles.loading}>
          <div className={styles.spinner} />
          <p>불러오는 중...</p>
        </div>
      </div>
    );
  }

  // 기록 없는 날 (404 에러)
  // error.response.status === 404 이면 기록이 없는 것
  const is404 = (error as any)?.response?.status === 404;
  if (is404 || !data) {
    return (
      <div className={styles.panel}>
        <div className={styles.dateHeader}>
          <h3>{dayjs(selectedDate).format('M월 D일 (ddd)')}</h3>
        </div>
        <div className={styles.empty}>
          <span className={styles.emptyIcon}>🏃</span>
          <p>이 날의 운동 기록이 없어요</p>
          <button className={styles.addBtn} onClick={onAdd}>
            + 운동 기록하기
          </button>
        </div>
      </div>
    );
  }

  // 기록 있는 날 - 상세 표시
  const handleDelete = async () => {
    if (!confirm('운동 기록을 삭제할까요?')) return;
    await deleteWorkout.mutateAsync(selectedDate);
  };

  return (
    <div className={styles.panel}>
      {/* 날짜 헤더 + 수정/삭제 버튼 */}
      <div className={styles.dateHeader}>
        <h3>{dayjs(selectedDate).format('M월 D일 (ddd)')}</h3>
        <div className={styles.actions}>
          <button className={styles.editBtn} onClick={onEdit}>수정</button>
          <button className={styles.deleteBtn} onClick={handleDelete}>삭제</button>
        </div>
      </div>

      {/* 메모 */}
      {data.memo && (
        <div className={styles.memo}>
          <span className={styles.memoIcon}>📝</span>
          {data.memo}
        </div>
      )}

      {/* 운동 종목 목록 */}
      <div className={styles.exercises}>
        {data.exercises.map((exercise, idx) => (
          <div key={exercise.id} className={styles.exerciseCard}>
            {/* 종목 헤더 */}
            <div className={styles.exerciseHeader}>
              <span className={styles.exerciseOrder}>{idx + 1}</span>
              <div>
                <span className={styles.exerciseName}>{exercise.name}</span>
                <span className={styles.bodyPart}>{exercise.bodyPart}</span>
              </div>
            </div>

            {/* 세트 테이블 */}
            <table className={styles.setTable}>
              <thead>
                <tr>
                  <th>세트</th>
                  <th>무게</th>
                  <th>횟수</th>
                  <th>완료</th>
                </tr>
              </thead>
              <tbody>
                {exercise.sets.map(set => (
                  <tr key={set.id} className={!set.completed ? styles.notCompleted : ''}>
                    <td>{set.setNumber}세트</td>
                    {/* 무게가 null이면 "맨몸" 표시 */}
                    <td>{set.weightKg != null ? `${set.weightKg}kg` : '맨몸'}</td>
                    <td>{set.reps}회</td>
                    <td>{set.completed ? '✅' : '❌'}</td>
                  </tr>
                ))}
              </tbody>
            </table>

            {/* 볼륨 요약: 총 볼륨 = 무게 × 횟수의 합 */}
            <div className={styles.volume}>
              총 볼륨:{' '}
              <strong>
                {exercise.sets
                  .filter(s => s.completed && s.weightKg != null)
                  .reduce((sum, s) => sum + (s.weightKg! * s.reps), 0)
                  .toLocaleString()}
                kg
              </strong>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
