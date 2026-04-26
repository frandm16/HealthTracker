import { Stack } from 'expo-router';
import * as React from 'react';
import { useCallback, useEffect, useMemo, useState } from 'react';
import { Pressable, ScrollView, Text, TextInput, View } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

import {
  addMealItem,
  fetchNutritionDay,
  fetchNutritionSummary,
} from '@/features/nutrition/api/nutrition-api';
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
  targetCaloriesKcal: 1800,
  targetProteinG: '140',
  targetCarbsG: '198',
  targetFatsG: '50',
};

function todayIsoDate() {
  return new Date().toISOString().slice(0, 10);
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

export default function NutritionScreen() {
  const insets = useSafeAreaInsets();
  const auth = React.use(AuthContext);
  const [selectedDay, setSelectedDay] = useState(todayIsoDate());
  const [selectedMealType, setSelectedMealType] = useState<MealType>('BREAKFAST');
  const [day, setDay] = useState<NutritionDayResponse | null>(null);
  const [summary, setSummary] = useState<NutritionSummaryResponse | null>(null);
  const [form, setForm] = useState(emptyForm);
  const [loading, setLoading] = useState(false);
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
  const caloriesLeft = Math.max(targetCalories - calories, 0);
  const caloriesProgress = targetCalories > 0 ? Math.min(calories / targetCalories, 1) : 0;

  const loadDay = useCallback(async () => {
    if (!session) {
      setDay(null);
      setSummary(null);
      return;
    }

    setLoading(true);
    setMessage(null);

    try {
      const [nextDay, nextSummary] = await withValidToken((accessToken) =>
        Promise.all([
          fetchNutritionDay(selectedDay, accessToken),
          fetchNutritionSummary(selectedDay, accessToken),
        ])
      );
      setDay(nextDay);
      setSummary(nextSummary);
    } catch (error) {
      setDay(null);
      setSummary(null);
      if (error instanceof ApiError && error.status === 403) {
        setMessage('Something went wrong.');
      } else if (error instanceof ApiError && error.status === 401) {
        setMessage('Please sign in.');
      } else {
        setMessage('Something went wrong.');
      }
    } finally {
      setLoading(false);
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
    <>
      <Stack.Screen options={{ title: 'Nutrition' }} />
      <ScrollView
        contentInsetAdjustmentBehavior="automatic"
        keyboardShouldPersistTaps="handled"
        contentContainerStyle={{
          paddingTop: 18,
          paddingRight: 16,
          paddingBottom: Math.max(insets.bottom, 28),
          paddingLeft: 16,
          gap: 16,
          backgroundColor: '#f4f7f2',
        }}
      >
        <View style={{ gap: 14 }}>
          <View style={{ flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', gap: 12 }}>
            <View style={{ gap: 4 }}>
              <Text selectable style={{ color: '#60705b', fontSize: 13, fontWeight: '800' }}>
                Hoy
              </Text>
              <Text selectable style={{ color: '#102114', fontSize: 30, fontWeight: '900' }}>
                Diary
              </Text>
            </View>
            <View style={{ minWidth: 126, gap: 6 }}>
              <TextInput
                value={selectedDay}
                onChangeText={setSelectedDay}
                placeholder="YYYY-MM-DD"
                autoCapitalize="none"
                style={{
                  minHeight: 42,
                  paddingHorizontal: 12,
                  borderRadius: 14,
                  borderCurve: 'continuous',
                  backgroundColor: '#ffffff',
                  color: '#102114',
                  fontSize: 13,
                  fontWeight: '800',
                }}
              />
            </View>
          </View>

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
            <View style={{ alignItems: 'center', gap: 8 }}>
              <Text selectable style={{ color: '#60705b', fontSize: 13, fontWeight: '800' }}>
                kcal left
              </Text>
              <Text selectable style={{ color: '#102114', fontSize: 48, fontWeight: '900', fontVariant: ['tabular-nums'] }}>
                {Math.round(caloriesLeft)}
              </Text>
              <Text selectable style={{ color: '#60705b', fontSize: 14, fontWeight: '700' }}>
                {Math.round(calories)} / {targetCalories} kcal
              </Text>
            </View>

            <View style={{ height: 12, borderRadius: 999, backgroundColor: '#e3ebde', overflow: 'hidden' }}>
              <View style={{ width: `${caloriesProgress * 100}%`, height: 12, borderRadius: 999, backgroundColor: '#72c14b' }} />
            </View>

            <View style={{ flexDirection: 'row', gap: 8 }}>
              <MacroPill label="Protein" current={summary?.proteinG} target={day?.targetProteinG ?? defaultDayRequest.targetProteinG}  />
              <MacroPill label="Carbs" current={summary?.carbsG} target={day?.targetCarbsG ?? defaultDayRequest.targetCarbsG}  />
              <MacroPill label="Fat" current={summary?.fatsG} target={day?.targetFatsG ?? defaultDayRequest.targetFatsG}  />
            </View>
          </View>
        </View>

        <View style={{ flexDirection: 'row', gap: 10 }}>
          <Pressable
            disabled={!session || loading}
            onPress={() => void loadDay()}
            style={{
              flex: 1,
              alignItems: 'center',
              justifyContent: 'center',
              minHeight: 48,
              borderRadius: 16,
              borderCurve: 'continuous',
              backgroundColor: session && !loading ? '#102114' : '#cdd8c7',
            }}
          >
            <Text style={{ color: '#ffffff', fontWeight: '900' }}>{loading ? 'Loading...' : 'Refresh'}</Text>
          </Pressable>
        </View>

        {signedOut ? <Notice tone="warning" text="Please sign in." /> : null}
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
          <Field label="Marca" value={form.brand} onChangeText={(brand) => setForm((current) => ({ ...current, brand }))} />
          <View style={{ flexDirection: 'row', gap: 10 }}>
            <Field label="Gramos" value={form.quantityG} keyboardType="decimal-pad" onChangeText={(quantityG) => setForm((current) => ({ ...current, quantityG }))} />
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
      </ScrollView>
    </>
  );
}

function MacroPill({ label, current, target }: { label: string; current: string | undefined; target: string | number;}) {
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
              {rounded(calories)} kcal • {rounded(protein)} P | {rounded(carbs)} C | {rounded(fat)} F
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
