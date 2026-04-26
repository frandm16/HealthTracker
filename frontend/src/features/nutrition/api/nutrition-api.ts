import { apiRequest } from '@/lib/api-client';
import type {
  MealItemRequest,
  MealItemResponse,
  MealType,
  NutritionDayRequest,
  NutritionDayResponse,
  NutritionSummaryResponse,
} from '@/features/nutrition/types';

export function fetchNutritionDay(day: string, accessToken: string): Promise<NutritionDayResponse> {
  return apiRequest<NutritionDayResponse>(`/api/nutrition/days/${day}`, {
    method: 'GET',
    accessToken,
  });
}

export function fetchNutritionSummary(day: string, accessToken: string): Promise<NutritionSummaryResponse> {
  return apiRequest<NutritionSummaryResponse>(`/api/nutrition/days/${day}/summary`, {
    method: 'GET',
    accessToken,
  });
}

export function upsertNutritionDay(
  day: string,
  request: NutritionDayRequest,
  accessToken: string
): Promise<NutritionDayResponse> {
  return apiRequest<NutritionDayResponse>(`/api/nutrition/days/${day}`, {
    method: 'PUT',
    accessToken,
    body: JSON.stringify(request),
  });
}

export function addMealItem(
  day: string,
  mealType: MealType,
  request: MealItemRequest,
  accessToken: string
): Promise<MealItemResponse> {
  return apiRequest<MealItemResponse>(`/api/nutrition/days/${day}/meals/${mealType}/items`, {
    method: 'POST',
    accessToken,
    body: JSON.stringify(request),
  });
}
