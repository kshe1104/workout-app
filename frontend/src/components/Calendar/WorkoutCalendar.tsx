/**
 * [components/Calendar/WorkoutCalendar.tsx - 운동 달력]
 *
 * 기능:
 * - 해당 월의 달력 표시
 * - 운동한 날: 초록색 점 + 운동 부위 표시
 * - 날짜 클릭: 상세 기록 패널 열기
 * - 이전/다음 달 이동
 *
 * [dayjs란?]
 * 날짜 계산을 쉽게 해주는 라이브러리입니다.
 * dayjs('2025-03-01').daysInMonth() → 31 (3월 일수)
 * dayjs('2025-03-01').day() → 6 (토요일, 0=일요일)
 */
import { useState } from 'react';
import dayjs from 'dayjs';
import { useCalendar } from '../../hooks/useWorkout';
import type { CalendarDay } from '../../types/workout';
import styles from './WorkoutCalendar.module.css';

interface Props {
  selectedDate: string | null;
  onDateSelect: (date: string) => void;
}

// 부위별 색상 매핑 (달력 셀에 작은 점으로 표시)
const BODY_PART_COLORS: Record<string, string> = {
  '가슴': '#ef4444',
  '등': '#3b82f6',
  '어깨': '#f59e0b',
  '이두': '#8b5cf6',
  '삼두': '#ec4899',
  '하체': '#10b981',
  '복근': '#f97316',
  '유산소': '#06b6d4',
};

export default function WorkoutCalendar({ selectedDate, onDateSelect }: Props) {
  // 현재 보고 있는 연/월 상태
  const [currentDate, setCurrentDate] = useState(dayjs());

  const year = currentDate.year();
  const month = currentDate.month() + 1; // dayjs: 0~11, 우리는 1~12

  // React Query로 달력 데이터 조회
  const { data, isLoading } = useCalendar(year, month);

  // 운동 기록을 날짜를 키로 하는 Map으로 변환 (빠른 조회를 위해)
  // { "2025-03-10": CalendarDay, "2025-03-15": CalendarDay, ... }
 const workoutMap = new Map<string, CalendarDay>(
  (data?.workoutDays ?? []).map(day => [day.date, day])
);

  // 달력 그리기 계산
  const firstDayOfMonth = currentDate.startOf('month').day(); // 1일이 무슨 요일인지 (0=일)
  const daysInMonth = currentDate.daysInMonth();              // 이 달의 총 일수

  // 달력 셀 배열 생성 (앞에 빈 칸 + 날짜들)
  const calendarCells: (number | null)[] = [
    ...Array(firstDayOfMonth).fill(null), // 1일 전의 빈 칸들
    ...Array.from({ length: daysInMonth }, (_, i) => i + 1), // 1~말일
  ];

  const handlePrevMonth = () => setCurrentDate(prev => prev.subtract(1, 'month'));
  const handleNextMonth = () => setCurrentDate(prev => prev.add(1, 'month'));

  const today = dayjs().format('YYYY-MM-DD');

  return (
    <div className={styles.calendar}>
      {/* 헤더: 연월 + 이전/다음 버튼 */}
      <div className={styles.header}>
        <button className={styles.navBtn} onClick={handlePrevMonth}>‹</button>
        <h2 className={styles.title}>
          {year}년 {month}월
        </h2>
        <button className={styles.navBtn} onClick={handleNextMonth}>›</button>
      </div>

      {/* 요일 헤더 */}
      <div className={styles.weekdays}>
        {['일', '월', '화', '수', '목', '금', '토'].map(day => (
          <div key={day} className={styles.weekday}>{day}</div>
        ))}
      </div>

      {/* 달력 날짜 셀 */}
      {isLoading ? (
        <div className={styles.loading}>로딩 중...</div>
      ) : (
        <div className={styles.grid}>
          {calendarCells.map((day, index) => {
            if (day === null) {
              // 빈 칸
              return <div key={`empty-${index}`} className={styles.emptyCell} />;
            }

            // 날짜 문자열 생성 ("2025-03-05" 형식)
            const dateStr = `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
            const workoutData = workoutMap.get(dateStr);
            const hasWorkout = workoutData?.hasWorkout ?? false;
            const isToday = dateStr === today;
            const isSelected = dateStr === selectedDate;

            return (
              <div
                key={dateStr}
                className={[
                  styles.dayCell,
                  hasWorkout ? styles.hasWorkout : '',
                  isToday ? styles.today : '',
                  isSelected ? styles.selected : '',
                ].join(' ')}
                onClick={() => onDateSelect(dateStr)}
              >
                <span className={styles.dayNumber}>{day}</span>

                {/* 운동한 날: 부위별 색상 점 표시 */}
                {hasWorkout && workoutData && (
                  <div className={styles.bodyPartDots}>
                    {workoutData.bodyParts.slice(0, 3).map(part => (
                      <span
                        key={part}
                        className={styles.dot}
                        style={{ backgroundColor: BODY_PART_COLORS[part] ?? '#6b7280' }}
                        title={part} // 마우스 올리면 부위 이름 보임
                      />
                    ))}
                    {/* 3개 초과 시 +N 표시 */}
                    {workoutData.bodyParts.length > 3 && (
                      <span className={styles.moreDots}>
                        +{workoutData.bodyParts.length - 3}
                      </span>
                    )}
                  </div>
                )}
              </div>
            );
          })}
        </div>
      )}

      {/* 범례: 부위별 색상 설명 */}
      <div className={styles.legend}>
        {Object.entries(BODY_PART_COLORS).map(([part, color]) => (
          <div key={part} className={styles.legendItem}>
            <span className={styles.legendDot} style={{ backgroundColor: color }} />
            <span>{part}</span>
          </div>
        ))}
      </div>
    </div>
  );
}
