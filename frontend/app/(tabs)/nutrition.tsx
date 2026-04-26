import * as React from 'react';
import { useCallback, useEffect, useMemo, useState } from 'react';
import { PanResponder, Pressable, RefreshControl, ScrollView, Text, TextInput, View } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

import { addMealItem, fetchNutritionDay, fetchNutritionSummary, upsertNutritionDay } from '@/features/nutrition/api/nutrition-api';
import type { MealSlotResponse, MealType, NutritionDayResponse, NutritionSummaryResponse } from '@/features/nutrition/types';
import { ApiError } from '@/lib/api-client';
import { AuthContext } from '@/providers/auth-provider';

const mealTypes: MealType[] = ['BREAKFAST', 'LUNCH', 'DINNER', 'SNACK'];

const mealLabels: Record<MealType, string> = {
  BREAKFAST: 'Breakfast',
  LUNCH: 'Lunch',
  DINNER: 'Dinner',
  SNACK: 'Snack',
};

const mealAccents: Record<MealType, string> = {
  BREAKFAST: '#f97316',
  LUNCH: '#16a34a',
  DINNER: '#2563eb',
  SNACK: '#a855f7',
};

const defaultDayRequest = {
  restingCaloriesKcal: 0,
  activeCaloriesKcal: 0,
  adjustmentCaloriesKcal: 0,
  targetCaloriesKcal: 2200,
  targetProteinG: '140',
  targetCarbsG: '198',
  targetFatsG: '50',
};

const weekdayLabels = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
const selectedDayFormatter = new Intl.DateTimeFormat('en-US', { month: 'short', day: 'numeric' });

function todayIsoDate() {
  return formatIsoDate(new Date());
}

function parseIsoDate(value: string) {
  const [year, month, day] = value.split('-').map(Number);
  return new Date(year, month - 1, day);
}

function formatIsoDate(date: Date) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

function addDays(date: Date, days: number) {
  const nextDate = new Date(date);
  nextDate.setDate(nextDate.getDate() + days);
  return nextDate;
}

function startOfMondayWeek(date: Date) {
  const day = date.getDay();
  const mondayOffset = day === 0 ? -6 : 1 - day;
  return addDays(date, mondayOffset);
}

function getMondayWeekDays(date: Date) {
  const weekStart = startOfMondayWeek(date);
  return weekdayLabels.map((weekday, index) => {
    const value = addDays(weekStart, index);
    return {
      weekday,
      date: value,
      isoDate: formatIsoDate(value),
      dayNumber: value.getDate(),
    };
  });
}

function numberOf(value: string | number | null | undefined) {
  const numeric = Number(value ?? 0);
  return Number.isFinite(numeric) ? numeric : 0;
}

function rounded(value: string | number | null | undefined) {
  return Math.round(numberOf(value)).toString();
}

function decimal(value: string | number | null | undefined) {
  return numberOf(value).toFixed(1);
}

function emptyForm() {
  return {
    foodName: '',
    brand: '',
    quantityG: '100',
    caloriesKcal: '0',
    proteinG: '0',
    carbsG: '0',
    fatsG: '0',
  };
}

function emptySummary(day: string): NutritionSummaryResponse {
  return {
    day,
    caloriesKcal: '0',
    proteinG: '0',
    carbsG: '0',
    fatsG: '0',
    waterMl: 0,
  };
}

function emptyMealSlot(day: string, mealType: MealType): MealSlotResponse {
  return {
    id: `${day}-${mealType}`,
    mealType,
    items: [],
    dishes: [],
  };
}

function emptyDay(day: string): NutritionDayResponse {
  return {
    id: day,
    day,
    mealSlots: mealTypes.map((mealType) => emptyMealSlot(day, mealType)),
    ...defaultDayRequest,
  };
}

export default function NutritionScreen() {
  const insets = useSafeAreaInsets();
  const auth = React.use(AuthContext);
  const [selectedDay, setSelectedDay] = useState(todayIsoDate());
  const [selectedMealType, setSelectedMealType] = useState<MealType>('BREAKFAST');
  const [day, setDay] = useState<NutritionDayResponse | null>(null);
  const [summary, setSummary] = useState<NutritionSummaryResponse | null>(null);
  const [form, setForm] = useState(emptyForm);
  const [loading, setLoading] = useState(false);
  const [refreshing, setRefreshing] = useState(false);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState<string | null>(null);

  if (!auth) {
    throw new Error('Auth context is missing.');
  }

  const { getAccessToken, session, status } = auth;

  const withValidToken = useCallback(
    async <T,>(request: (accessToken: string) => Promise<T>): Promise<T> => {
      try {
        return await request(await getAccessToken());
      } catch (error) {
        if (error instanceof ApiError && error.status === 401) {
          return request(await getAccessToken(true));
        }

        throw error;
      }
    },
    [getAccessToken]
  );

  const slotsByType = useMemo(() => {
    const slots = new Map<MealType, MealSlotResponse>();
    day?.mealSlots.forEach((slot) => slots.set(slot.mealType, slot));
    return slots;
  }, [day]);

  const targetCalories = day?.targetCaloriesKcal ?? defaultDayRequest.targetCaloriesKcal;
  const calories = numberOf(summary?.caloriesKcal);
  const caloriesProgress = targetCalories > 0 ? Math.min(calories / targetCalories, 1) : 0;
  const selectedDate = useMemo(() => parseIsoDate(selectedDay), [selectedDay]);
  const weekDays = useMemo(() => getMondayWeekDays(selectedDate), [selectedDate]);
  const selectedDayLabel = useMemo(() => selectedDayFormatter.format(selectedDate), [selectedDate]);
  const today = todayIsoDate();

  const selectDay = useCallback((nextDay: string) => {
    setSelectedDay(nextDay);
    setDay(emptyDay(nextDay));
    setSummary(emptySummary(nextDay));
    setMessage(null);
  }, []);

  const weekPanResponder = useMemo(
    () =>
      PanResponder.create({
        onMoveShouldSetPanResponder: (_, gestureState) =>
          Math.abs(gestureState.dx) > 18 && Math.abs(gestureState.dx) > Math.abs(gestureState.dy),
        onPanResponderRelease: (_, gestureState) => {
          if (Math.abs(gestureState.dx) < 48) {
            return;
          }

          selectDay(formatIsoDate(addDays(selectedDate, gestureState.dx < 0 ? 7 : -7)));
        },
      }),
    [selectDay, selectedDate]
  );

  const dayPanResponder = useMemo(
    () =>
      PanResponder.create({
        onMoveShouldSetPanResponder: (_, gestureState) =>
          Math.abs(gestureState.dx) > 28 && Math.abs(gestureState.dx) > Math.abs(gestureState.dy) * 1.4,
        onPanResponderRelease: (_, gestureState) => {
          if (Math.abs(gestureState.dx) < 56) {
            return;
          }

          selectDay(formatIsoDate(addDays(selectedDate, gestureState.dx < 0 ? 1 : -1)));
        },
      }),
    [selectDay, selectedDate]
  );

  const loadDay = useCallback(async (mode: 'load' | 'refresh' = 'load') => {
    if (!session) {
      setDay(null);
      setSummary(null);
      return;
    }

    if (mode === 'refresh') {
      setRefreshing(true);
    } else {
      setLoading(true);
    }
    setMessage(null);

    try {
      const [nextDay, nextSummary] = await withValidToken(async (accessToken) => {
        try {
          return await Promise.all([fetchNutritionDay(selectedDay, accessToken), fetchNutritionSummary(selectedDay, accessToken)]);
        } catch (error) {
          if (error instanceof ApiError && error.status === 404) {
            const createdDay = await upsertNutritionDay(selectedDay, defaultDayRequest, accessToken);
            let createdSummary: NutritionSummaryResponse;

            try {
              createdSummary = await fetchNutritionSummary(selectedDay, accessToken);
            } catch {
              createdSummary = emptySummary(selectedDay);
            }

            return [createdDay, createdSummary] as const;
          }

          throw error;
        }
      });
      setDay(nextDay);
      setSummary(nextSummary);
    } catch (error) {
      if (error instanceof ApiError && error.status === 401) {
        setDay(null);
        setSummary(null);
        setMessage('Please sign in.');
      } else {
        setMessage('Something went wrong.');
      }
    } finally {
      if (mode === 'refresh') {
        setRefreshing(false);
      } else {
        setLoading(false);
      }
    }
  }, [selectedDay, session, withValidToken]);

  useEffect(() => {
    void loadDay();
  }, [loadDay]);

  async function handleAddItem() {
    if (!session) {
      setMessage('Please sign in.');
      return;
    }

    if (!day) {
      setMessage('Something went wrong.');
      return;
    }

    if (!form.foodName.trim()) {
      setMessage('Enter a food name.');
      return;
    }

    setSaving(true);
    setMessage(null);

    try {
      await withValidToken((accessToken) =>
        addMealItem(
          selectedDay,
          selectedMealType,
          {
            foodId: null,
            foodName: form.foodName.trim(),
            brand: form.brand.trim() || null,
            quantityG: form.quantityG,
            caloriesKcal: form.caloriesKcal,
            proteinG: form.proteinG,
            carbsG: form.carbsG,
            fatsG: form.fatsG,
          },
          accessToken
        )
      );
      setForm(emptyForm());
      await loadDay();
      setMessage('Saved.');
    } catch {
      setMessage('Something went wrong.');
    } finally {
      setSaving(false);
    }
  }

  const signedOut = status !== 'loading' && !session;

  return (
    <View style={{ flex: 1, backgroundColor: '#f4f7f2' }}>
      <View
        style={{
          gap: 14,
          paddingTop: 0,
          paddingRight: 16,
          paddingBottom: 10,
          paddingLeft: 16,
        }}
      >
        <View style={{ alignItems: 'center' }}>
          <Pressable
            onPress={() => selectDay(today)}
            style={{
              paddingHorizontal: 14,
              paddingVertical: 8,
              borderRadius: 999,
              borderCurve: 'continuous',
              backgroundColor: '#ffffff',
              boxShadow: '0 6px 14px rgba(16, 33, 20, 0.06)',
            }}
          >
            <Text
              selectable
              style={{
                color: '#102114',
                fontSize: 17,
                fontWeight: '900',
              }}
            >
              {selectedDayLabel}
            </Text>
          </Pressable>
        </View>

        <View
          {...weekPanResponder.panHandlers}
          style={{
            flexDirection: 'row',
            justifyContent: 'space-between',
            gap: 6,
            padding: 10,
            borderRadius: 22,
            borderCurve: 'continuous',
            backgroundColor: '#ffffff',
            boxShadow: '0 8px 18px rgba(16, 33, 20, 0.06)',
          }}
        >
          {weekDays.map((weekDay) => {
            const selected = weekDay.isoDate === selectedDay;
            const isToday = weekDay.isoDate === today;

            return (
              <Pressable
                key={weekDay.isoDate}
                onPress={() => selectDay(weekDay.isoDate)}
                style={{
                  flex: 1,
                  alignItems: 'center',
                  gap: 7,
                  paddingVertical: 6,
                }}
              >
                <Text
                  selectable
                  style={{
                    color: selected ? '#72c14b' : '#60705b',
                    fontSize: 11,
                    fontWeight: '900',
                  }}
                >
                  {weekDay.weekday}
                </Text>
                <View
                  style={{
                    width: 38,
                    height: 38,
                    alignItems: 'center',
                    justifyContent: 'center',
                    borderRadius: 999,
                    borderWidth: selected || isToday ? 2 : 0,
                    borderColor: selected ? '#72c14b' : '#b7d9a4',
                    backgroundColor: selected ? '#72c14b' : '#f6f8f4',
                  }}
                >
                  <Text
                    selectable
                    style={{
                      color: selected ? '#ffffff' : '#102114',
                      fontSize: 15,
                      fontWeight: '900',
                      fontVariant: ['tabular-nums'],
                    }}
                  >
                    {weekDay.dayNumber}
                  </Text>
                </View>
              </Pressable>
            );
          })}
        </View>
      </View>

      <ScrollView
        contentInsetAdjustmentBehavior="automatic"
        keyboardShouldPersistTaps="handled"
        contentContainerStyle={{
          paddingTop: 4,
          paddingRight: 16,
          paddingBottom: Math.max(insets.bottom, 28),
          paddingLeft: 16,
        }}
        refreshControl={
          <RefreshControl
            refreshing={refreshing}
            onRefresh={() => void loadDay('refresh')}
            tintColor="#72c14b"
            colors={['#72c14b']}
            progressBackgroundColor="#ffffff"
          />
        }
      >
        <View {...dayPanResponder.panHandlers} style={{ gap: 16 }}>
          <View
            style={{
              gap: 18,
              padding: 18,
              borderRadius: 22,
              borderCurve: 'continuous',
              backgroundColor: '#ffffff',
              boxShadow: '0 12px 30px rgba(16, 33, 20, 0.08)',
            }}
          >
            <View style={{ alignItems: 'center' }}>
              <Text selectable style={{ color: '#102114', fontSize: 24, fontWeight: '900', fontVariant: ['tabular-nums'] }}>
                {Math.round(calories)} / {targetCalories}
              </Text>
              <Text selectable style={{ color: '#60705b', fontSize: 14, fontWeight: '700' }}>
                kcal
              </Text>
            </View>

            <View style={{ height: 12, borderRadius: 999, backgroundColor: '#e3ebde', overflow: 'hidden' }}>
              <View style={{ width: `${caloriesProgress * 100}%`, height: 12, borderRadius: 999, backgroundColor: '#72c14b' }} />
            </View>

            <View style={{ flexDirection: 'row', gap: 8 }}>
              <MacroPill label="Protein" current={summary?.proteinG} target={day?.targetProteinG ?? defaultDayRequest.targetProteinG} />
              <MacroPill label="Carbs" current={summary?.carbsG} target={day?.targetCarbsG ?? defaultDayRequest.targetCarbsG} />
              <MacroPill label="Fat" current={summary?.fatsG} target={day?.targetFatsG ?? defaultDayRequest.targetFatsG} />
            </View>
          </View>

          {signedOut ? <Notice tone="warning" text="Please sign in." /> : null}
          {loading ? <Notice tone="info" text="Loading..." /> : null}
          {message ? <Notice tone={message === 'Saved.' ? 'success' : 'info'} text={message} /> : null}

          <View style={{ gap: 10 }}>
            {mealTypes.map((mealType) => (
              <MealCard key={mealType} mealType={mealType} slot={slotsByType.get(mealType)} />
            ))}
          </View>

          <View
            style={{
              gap: 12,
              padding: 16,
              borderRadius: 22,
              borderCurve: 'continuous',
              backgroundColor: '#ffffff',
              boxShadow: '0 8px 18px rgba(16, 33, 20, 0.06)',
            }}
          >
            <Text selectable style={{ color: '#102114', fontSize: 20, fontWeight: '900' }}>
              Add food
            </Text>
            <View style={{ flexDirection: 'row', flexWrap: 'wrap', gap: 8 }}>
              {mealTypes.map((mealType) => (
                <Pressable
                  key={mealType}
                  onPress={() => setSelectedMealType(mealType)}
                  style={{
                    paddingHorizontal: 12,
                    paddingVertical: 9,
                    borderRadius: 999,
                    backgroundColor: selectedMealType === mealType ? mealAccents[mealType] : '#eef3ea',
                  }}
                >
                  <Text style={{ color: selectedMealType === mealType ? '#ffffff' : '#40513b', fontWeight: '900' }}>
                    {mealLabels[mealType]}
                  </Text>
                </Pressable>
              ))}
            </View>
            <Field label="Food" value={form.foodName} onChangeText={(foodName) => setForm((current) => ({ ...current, foodName }))} />
            <Field label="Brand" value={form.brand} onChangeText={(brand) => setForm((current) => ({ ...current, brand }))} />
            <View style={{ flexDirection: 'row', gap: 10 }}>
              <Field label="Grams" value={form.quantityG} keyboardType="decimal-pad" onChangeText={(quantityG) => setForm((current) => ({ ...current, quantityG }))} />
              <Field label="Kcal" value={form.caloriesKcal} keyboardType="decimal-pad" onChangeText={(caloriesKcal) => setForm((current) => ({ ...current, caloriesKcal }))} />
            </View>
            <View style={{ flexDirection: 'row', gap: 10 }}>
              <Field label="Prot." value={form.proteinG} keyboardType="decimal-pad" onChangeText={(proteinG) => setForm((current) => ({ ...current, proteinG }))} />
              <Field label="Carbs" value={form.carbsG} keyboardType="decimal-pad" onChangeText={(carbsG) => setForm((current) => ({ ...current, carbsG }))} />
              <Field label="Fat" value={form.fatsG} keyboardType="decimal-pad" onChangeText={(fatsG) => setForm((current) => ({ ...current, fatsG }))} />
            </View>
            <Pressable
              disabled={!session || !day || saving}
              onPress={handleAddItem}
              style={{
                alignItems: 'center',
                padding: 16,
                borderRadius: 16,
                borderCurve: 'continuous',
                backgroundColor: session && day && !saving ? '#72c14b' : '#cde8bd',
              }}
            >
              <Text style={{ color: '#ffffff', fontSize: 16, fontWeight: '900' }}>
                {saving ? 'Saving...' : 'Add to diary'}
              </Text>
            </Pressable>
          </View>
        </View>
      </ScrollView>
    </View>
  );
}

function MacroPill({ label, current, target }: { label: string; current: string | undefined; target: string | number }) {
  return (
    <View style={{ flex: 1, gap: 6, padding: 10, borderRadius: 16, borderCurve: 'continuous', backgroundColor: '#f6f8f4' }}>
      <Text selectable style={{ color: '#60705b', fontSize: 11, fontWeight: '800' }}>
        {label}
      </Text>
      <Text selectable style={{ color: '#102114', fontSize: 13, fontWeight: '900', fontVariant: ['tabular-nums'] }}>
        {rounded(current)} / {rounded(target)}g
      </Text>
    </View>
  );
}

function MealCard({ mealType, slot }: { mealType: MealType; slot: MealSlotResponse | undefined }) {
  const calories = slot?.items.reduce((total, item) => total + numberOf(item.caloriesKcal), 0) ?? 0;
  const protein = slot?.items.reduce((total, item) => total + numberOf(item.proteinG), 0) ?? 0;
  const carbs = slot?.items.reduce((total, item) => total + numberOf(item.carbsG), 0) ?? 0;
  const fat = slot?.items.reduce((total, item) => total + numberOf(item.fatsG), 0) ?? 0;

  return (
    <View
      style={{
        gap: 10,
        padding: 16,
        borderRadius: 22,
        borderCurve: 'continuous',
        backgroundColor: '#ffffff',
        boxShadow: '0 8px 18px rgba(16, 33, 20, 0.06)',
      }}
    >
      <View style={{ flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', gap: 12 }}>
        <View style={{ flexDirection: 'row', alignItems: 'center', gap: 10 }}>
          <View>
            <Text selectable style={{ color: '#102114', fontSize: 18, fontWeight: '900' }}>
              {mealLabels[mealType]}
            </Text>
            <Text selectable style={{ color: '#60705b', fontSize: 13, fontWeight: '700' }}>
              {rounded(calories)} kcal - {rounded(protein)} P | {rounded(carbs)} C | {rounded(fat)} F
            </Text>
          </View>
        </View>
      </View>

      {slot?.items.length ? (
        slot.items.map((item) => (
          <View key={item.id} style={{ flexDirection: 'row', justifyContent: 'space-between', gap: 12, paddingVertical: 8 }}>
            <View style={{ flex: 1 }}>
              <Text selectable style={{ color: '#102114', fontSize: 15, fontWeight: '800' }}>
                {item.foodName}
              </Text>
              <Text selectable style={{ color: '#60705b', fontSize: 13 }}>
                {decimal(item.quantityG)} g
              </Text>
            </View>
            <Text selectable style={{ color: '#102114', fontSize: 15, fontWeight: '900', fontVariant: ['tabular-nums'] }}>
              {rounded(item.caloriesKcal)} kcal
            </Text>
          </View>
        ))
      ) : (
        <Text selectable style={{ color: '#9aa895', fontWeight: '700' }}>
          No food logged.
        </Text>
      )}
    </View>
  );
}

function Notice({ tone, text }: { tone: 'info' | 'success' | 'warning'; text: string }) {
  const colors = {
    info: ['#edf4ff', '#1d4ed8'],
    success: ['#edfbea', '#15803d'],
    warning: ['#fff7ed', '#c2410c'],
  } as const;

  return (
    <View style={{ padding: 14, borderRadius: 16, borderCurve: 'continuous', backgroundColor: colors[tone][0] }}>
      <Text selectable style={{ color: colors[tone][1], lineHeight: 20, fontWeight: '800' }}>
        {text}
      </Text>
    </View>
  );
}

function Field({
  label,
  value,
  keyboardType,
  onChangeText,
}: {
  label: string;
  value: string;
  keyboardType?: 'default' | 'decimal-pad';
  onChangeText: (value: string) => void;
}) {
  return (
    <View style={{ flex: 1, gap: 6 }}>
      <Text selectable style={{ color: '#60705b', fontSize: 11, fontWeight: '900', textTransform: 'uppercase' }}>
        {label}
      </Text>
      <TextInput
        value={value}
        onChangeText={onChangeText}
        keyboardType={keyboardType}
        style={{
          minHeight: 46,
          paddingHorizontal: 12,
          borderRadius: 14,
          borderCurve: 'continuous',
          borderWidth: 1,
          borderColor: '#d9e4d3',
          backgroundColor: '#fbfdf9',
          color: '#102114',
          fontWeight: '800',
        }}
      />
    </View>
  );
}
