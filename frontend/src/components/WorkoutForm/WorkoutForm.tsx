import { useState } from 'react';
import { useBodyParts, useExercises, useSaveWorkout, useUpdateWorkout } from '../../hooks/useWorkout';
import { exerciseApi } from '../../api';
import type { WorkoutDetail, WorkoutSaveRequest, ExerciseSaveRequest, SetSaveRequest } from '../../types/workout';
import styles from './WorkoutForm.module.css';
import dayjs from 'dayjs';

interface Props {
  date: string;
  existingData?: WorkoutDetail | null;
  onClose: () => void;
  onSuccess: () => void;
}

interface SetFormData {
  setNumber: number;
  weightKg: string;
  reps: string;
  completed: boolean;
}

interface ExerciseFormData {
  exerciseId: number;
  exerciseName: string;
  bodyPartName: string;
  orderIndex: number;
  sets: SetFormData[];
}

const FAVORITES_KEY = 'workout_favorites';

function getFavorites(): number[] {
  try {
    return JSON.parse(localStorage.getItem(FAVORITES_KEY) ?? '[]');
  } catch {
    return [];
  }
}

function saveFavorites(ids: number[]) {
  localStorage.setItem(FAVORITES_KEY, JSON.stringify(ids));
}

function initFormData(existingData: WorkoutDetail | null | undefined): ExerciseFormData[] {
  if (!existingData) return [];
  return existingData.exercises.map(ex => ({
    exerciseId: ex.id,
    exerciseName: ex.name,
    bodyPartName: ex.bodyPart,
    orderIndex: ex.orderIndex,
    sets: ex.sets.map(s => ({
      setNumber: s.setNumber,
      weightKg: s.weightKg?.toString() ?? '',
      reps: s.reps.toString(),
      completed: s.completed,
    })),
  }));
}

export default function WorkoutForm({ date, existingData, onClose, onSuccess }: Props) {
  const isEditMode = !!existingData;

  const [memo, setMemo] = useState(existingData?.memo ?? '');
  const [exercises, setExercises] = useState<ExerciseFormData[]>(initFormData(existingData));
  const [selectedBodyPartId, setSelectedBodyPartId] = useState<number | undefined>();
  const [showExercisePicker, setShowExercisePicker] = useState(false);
  const [favorites, setFavorites] = useState<number[]>(getFavorites);
  const [pickerTab, setPickerTab] = useState<'all' | 'favorites'>('all');
  const [showCustomInput, setShowCustomInput] = useState(false);
  const [customName, setCustomName] = useState('');
  const [customBodyPartId, setCustomBodyPartId] = useState<number | undefined>();
  const [isAddingCustom, setIsAddingCustom] = useState(false);

  const { data: bodyParts } = useBodyParts();
  const { data: exerciseList } = useExercises(pickerTab === 'favorites' ? undefined : selectedBodyPartId);
  const saveWorkout = useSaveWorkout();
  const updateWorkout = useUpdateWorkout(date);

  const displayedExercises = pickerTab === 'favorites'
    ? (exerciseList ?? []).filter(ex => favorites.includes(ex.id))
    : (exerciseList ?? []);

  const toggleFavorite = (e: React.MouseEvent, exerciseId: number) => {
    e.stopPropagation();
    const updated = favorites.includes(exerciseId)
      ? favorites.filter(id => id !== exerciseId)
      : [...favorites, exerciseId];
    setFavorites(updated);
    saveFavorites(updated);
  };

  const handleAddExercise = (exerciseId: number, name: string, bodyPartName: string) => {
    setExercises(prev => [...prev, {
      exerciseId,
      exerciseName: name,
      bodyPartName,
      orderIndex: prev.length + 1,
      sets: [1, 2, 3].map(n => ({
        setNumber: n, weightKg: '', reps: '', completed: true
      })),
    }]);
    setShowExercisePicker(false);
    setShowCustomInput(false);
    setCustomName('');
    setCustomBodyPartId(undefined);
  };

  const handleAddCustomExercise = async () => {
    if (!customName.trim()) { alert('종목 이름을 입력해주세요'); return; }
    if (!customBodyPartId) { alert('운동 부위를 선택해주세요'); return; }
    setIsAddingCustom(true);
    try {
      const res = await exerciseApi.addCustom(customName.trim(), customBodyPartId);
      const newEx = res.data;
      const bpName = bodyParts?.find(b => b.id === customBodyPartId)?.name ?? '';
      handleAddExercise(newEx.id, newEx.name, bpName);
    } catch (err: any) {
      alert(err.response?.data?.message ?? '종목 추가에 실패했습니다');
    } finally {
      setIsAddingCustom(false);
    }
  };

  const handleRemoveExercise = (index: number) => {
    setExercises(prev => prev.filter((_, i) => i !== index)
      .map((ex, i) => ({ ...ex, orderIndex: i + 1 })));
  };

  const handleAddSet = (exerciseIndex: number) => {
    setExercises(prev => prev.map((ex, i) => {
      if (i !== exerciseIndex) return ex;
      const lastSet = ex.sets[ex.sets.length - 1];
      return {
        ...ex,
        sets: [...ex.sets, {
          setNumber: ex.sets.length + 1,
          weightKg: lastSet?.weightKg ?? '',
          reps: lastSet?.reps ?? '',
          completed: true,
        }],
      };
    }));
  };

  const handleRemoveSet = (exerciseIndex: number, setIndex: number) => {
    setExercises(prev => prev.map((ex, i) => {
      if (i !== exerciseIndex) return ex;
      return {
        ...ex,
        sets: ex.sets
          .filter((_, si) => si !== setIndex)
          .map((s, si) => ({ ...s, setNumber: si + 1 })),
      };
    }));
  };

  const handleSetChange = (
    exerciseIndex: number,
    setIndex: number,
    field: keyof SetFormData,
    value: string | boolean
  ) => {
    setExercises(prev => prev.map((ex, i) => {
      if (i !== exerciseIndex) return ex;
      return {
        ...ex,
        sets: ex.sets.map((s, si) =>
          si === setIndex ? { ...s, [field]: value } : s
        ),
      };
    }));
  };

  const handleSubmit = async () => {
    if (exercises.length === 0) {
      alert('운동 종목을 1개 이상 추가해주세요');
      return;
    }
    const exerciseRequests: ExerciseSaveRequest[] = exercises.map(ex => ({
      exerciseId: ex.exerciseId,
      orderIndex: ex.orderIndex,
      sets: ex.sets.map(s => ({
        setNumber: s.setNumber,
        weightKg: s.weightKg !== '' ? parseFloat(s.weightKg) : null,
        reps: parseInt(s.reps) || 0,
        completed: s.completed,
      } as SetSaveRequest)),
    }));

    const request: WorkoutSaveRequest = { date, memo, exercises: exerciseRequests };
    try {
      if (isEditMode) {
        await updateWorkout.mutateAsync(request);
      } else {
        await saveWorkout.mutateAsync(request);
      }
      onSuccess();
    } catch (err: any) {
      alert(err.response?.data?.message ?? '저장에 실패했습니다');
    }
  };

  const isSaving = saveWorkout.isPending || updateWorkout.isPending;

  return (
    <div className={styles.overlay}>
      <div className={styles.modal}>
        {/* 헤더 - sticky */}
        <div className={styles.header}>
          <h2>{dayjs(date).format('M월 D일')} 운동 기록</h2>
          <button className={styles.closeBtn} onClick={onClose}>✕</button>
        </div>

        {/* 스크롤 가능한 본문 */}
        <div className={styles.scrollBody}>
          {/* 메모 */}
          <div className={styles.section}>
            <label className={styles.label}>메모 (선택)</label>
            <textarea
              className={styles.textarea}
              placeholder="오늘 운동 메모..."
              value={memo}
              onChange={e => setMemo(e.target.value)}
              rows={2}
            />
          </div>

          {/* 운동 종목 */}
          <div className={styles.section}>
            <div className={styles.sectionHeader}>
              <label className={styles.label}>운동 종목</label>
              <button
                className={styles.addExerciseBtn}
                onClick={() => { setShowExercisePicker(true); setShowCustomInput(false); }}
              >
                + 종목 추가
              </button>
            </div>

            {exercises.map((exercise, exIdx) => (
              <div key={exIdx} className={styles.exerciseBlock}>
                <div className={styles.exerciseBlockHeader}>
                  <span className={styles.exerciseBlockName}>
                    {exIdx + 1}. {exercise.exerciseName}
                    <span className={styles.exerciseBlockBodyPart}>{exercise.bodyPartName}</span>
                  </span>
                  <button className={styles.removeExerciseBtn} onClick={() => handleRemoveExercise(exIdx)}>✕</button>
                </div>

                <table className={styles.setInputTable}>
                  <thead>
                    <tr>
                      <th>세트</th><th>무게 (kg)</th><th>횟수</th><th>완료</th><th></th>
                    </tr>
                  </thead>
                  <tbody>
                    {exercise.sets.map((set, setIdx) => (
                      <tr key={setIdx}>
                        <td className={styles.setNumber}>{set.setNumber}</td>
                        <td>
                          <input type="number" className={styles.setInput} placeholder="맨몸"
                            value={set.weightKg} min="0" step="0.5"
                            onChange={e => handleSetChange(exIdx, setIdx, 'weightKg', e.target.value)} />
                        </td>
                        <td>
                          <input type="number" className={styles.setInput} placeholder="0"
                            value={set.reps} min="1"
                            onChange={e => handleSetChange(exIdx, setIdx, 'reps', e.target.value)} />
                        </td>
                        <td>
                          <input type="checkbox" checked={set.completed}
                            onChange={e => handleSetChange(exIdx, setIdx, 'completed', e.target.checked)} />
                        </td>
                        <td>
                          {exercise.sets.length > 1 && (
                            <button className={styles.removeSetBtn} onClick={() => handleRemoveSet(exIdx, setIdx)}>✕</button>
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
                <button className={styles.addSetBtn} onClick={() => handleAddSet(exIdx)}>+ 세트 추가</button>
              </div>
            ))}

            {/* ── 종목 선택 피커 (인라인) ── */}
            {showExercisePicker && (
              <div className={styles.exercisePicker}>
                <div className={styles.pickerHeader}>
                  <h3>운동 종목 선택</h3>
                  <button onClick={() => { setShowExercisePicker(false); setShowCustomInput(false); }}>✕</button>
                </div>

                {/* 전체 / 즐겨찾기 상단 탭 */}
                <div className={styles.pickerTopTabs}>
                  <button
                    className={pickerTab === 'all' ? styles.pickerTopTabActive : styles.pickerTopTab}
                    onClick={() => setPickerTab('all')}
                  >전체 종목</button>
                  <button
                    className={pickerTab === 'favorites' ? styles.pickerTopTabActive : styles.pickerTopTab}
                    onClick={() => setPickerTab('favorites')}
                  >⭐ 즐겨찾기</button>
                </div>

                {/* 부위 필터 (전체 탭만) */}
                {pickerTab === 'all' && (
                  <div className={styles.bodyPartTabs}>
                    <button
                      className={!selectedBodyPartId ? styles.activeTab : styles.tab}
                      onClick={() => setSelectedBodyPartId(undefined)}
                    >전체</button>
                    {bodyParts?.map(bp => (
                      <button
                        key={bp.id}
                        className={selectedBodyPartId === bp.id ? styles.activeTab : styles.tab}
                        onClick={() => setSelectedBodyPartId(bp.id)}
                      >{bp.name}</button>
                    ))}
                  </div>
                )}

                {/* 종목 리스트 */}
                <div className={styles.exerciseList}>
                  {displayedExercises.length === 0 && (
                    <p className={styles.emptyMsg}>
                      {pickerTab === 'favorites' ? '즐겨찾기한 종목이 없어요 ☆를 눌러 추가해보세요' : '종목이 없습니다'}
                    </p>
                  )}
                  {displayedExercises.map(ex => (
                    <button
                      key={ex.id}
                      className={styles.exerciseItem}
                      onClick={() => handleAddExercise(ex.id, ex.name, ex.bodyPartName)}
                    >
                      <span className={styles.exerciseItemLeft}>
                        <span
                          className={`${styles.starBtn} ${favorites.includes(ex.id) ? styles.starActive : ''}`}
                          onClick={e => toggleFavorite(e, ex.id)}
                          title="즐겨찾기"
                        >
                          {favorites.includes(ex.id) ? '★' : '☆'}
                        </span>
                        <span>{ex.name}</span>
                      </span>
                      <span className={styles.exerciseItemBodyPart}>{ex.bodyPartName}</span>
                    </button>
                  ))}
                </div>

                {/* 직접 입력 토글 */}
                <div className={styles.customToggleRow}>
                  <button
                    className={styles.customToggleBtn}
                    onClick={() => setShowCustomInput(v => !v)}
                  >
                    {showCustomInput ? '▲ 닫기' : '+ 목록에 없는 종목 직접 추가'}
                  </button>
                </div>

                {/* 직접 입력 영역 */}
                {showCustomInput && (
                  <div className={styles.customInputArea}>
                    <input
                      className={styles.customNameInput}
                      type="text"
                      placeholder="종목 이름 (예: 케이블 컬)"
                      value={customName}
                      onChange={e => setCustomName(e.target.value)}
                      onKeyDown={e => e.key === 'Enter' && handleAddCustomExercise()}
                    />
                    <select
                      className={styles.customBodyPartSelect}
                      value={customBodyPartId ?? ''}
                      onChange={e => setCustomBodyPartId(Number(e.target.value) || undefined)}
                    >
                      <option value="">부위 선택</option>
                      {bodyParts?.map(bp => (
                        <option key={bp.id} value={bp.id}>{bp.name}</option>
                      ))}
                    </select>
                    <button
                      className={styles.customAddBtn}
                      onClick={handleAddCustomExercise}
                      disabled={isAddingCustom}
                    >
                      {isAddingCustom ? '추가 중...' : '추가하기'}
                    </button>
                  </div>
                )}
              </div>
            )}
          </div>
        </div>{/* /scrollBody */}

        {/* 하단 버튼 - sticky */}
        <div className={styles.footer}>
          <button className={styles.cancelBtn} onClick={onClose}>취소</button>
          <button className={styles.saveBtn} onClick={handleSubmit} disabled={isSaving}>
            {isSaving ? '저장 중...' : (isEditMode ? '수정 완료' : '기록 저장')}
          </button>
        </div>
      </div>
    </div>
  );
}
