/**
 * [pages/DashboardPage.tsx - 메인 대시보드]
 *
 * 달력(왼쪽) + 운동 상세(오른쪽) 레이아웃
 *
 * [상태 관리 전략]
 * - selectedDate: 어떤 날짜가 선택됐는지 (달력 ↔ 상세 패널 통신)
 * - showForm: 운동 기록 입력 폼 표시 여부
 * - isEditMode: 수정 모드 여부
 *
 * selectedDate는 DashboardPage가 관리하고
 * WorkoutCalendar와 WorkoutDetailPanel에 props로 전달합니다.
 * → 두 컴포넌트가 서로 직접 통신하지 않고 부모를 통해 통신 (단방향 데이터 흐름)
 */
import { useState } from 'react';
import Navbar from '../components/common/Navbar';
import WorkoutCalendar from '../components/Calendar/WorkoutCalendar';
import WorkoutDetailPanel from '../components/WorkoutDetail/WorkoutDetailPanel';
import WorkoutForm from '../components/WorkoutForm/WorkoutForm';
import { useWorkoutDetail } from '../hooks/useWorkout';
import styles from './DashboardPage.module.css';

export default function DashboardPage() {
  const [selectedDate, setSelectedDate] = useState<string | null>(null);
  const [showForm, setShowForm] = useState(false);
  const [isEditMode, setIsEditMode] = useState(false);

  // 선택된 날짜의 데이터 조회 (수정 폼에서 초기값으로 사용)
  const { data: workoutDetail } = useWorkoutDetail(selectedDate);

  // 날짜 선택 핸들러
  const handleDateSelect = (date: string) => {
    setSelectedDate(date);
    setShowForm(false); // 날짜 바꾸면 폼 닫기
  };

  // 기록 추가 (폼 열기, 신규 모드)
  const handleAddWorkout = () => {
    setIsEditMode(false);
    setShowForm(true);
  };

  // 기록 수정 (폼 열기, 수정 모드)
  const handleEditWorkout = () => {
    setIsEditMode(true);
    setShowForm(true);
  };

  // 폼 저장 성공 후
  const handleFormSuccess = () => {
    setShowForm(false);
    // 캐시 무효화는 useSaveWorkout/useUpdateWorkout 훅에서 자동 처리됨
  };

  return (
    <div className={styles.page}>
      <Navbar />

      <main className={styles.main}>
        {/* 왼쪽: 달력 */}
        <section className={styles.calendarSection}>
          <WorkoutCalendar
            selectedDate={selectedDate}
            onDateSelect={handleDateSelect}
          />
        </section>

        {/* 오른쪽: 운동 상세 */}
        <section className={styles.detailSection}>
          <WorkoutDetailPanel
            selectedDate={selectedDate}
            onEdit={handleEditWorkout}
            onAdd={handleAddWorkout}
          />
        </section>
      </main>

      {/* 운동 기록 폼 (모달) */}
      {showForm && selectedDate && (
        <WorkoutForm
          date={selectedDate}
          existingData={isEditMode ? workoutDetail : null}
          onClose={() => setShowForm(false)}
          onSuccess={handleFormSuccess}
        />
      )}
    </div>
  );
}
