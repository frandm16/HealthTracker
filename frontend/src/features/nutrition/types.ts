export type MealType = 'BREAKFAST' | 'LUNCH' | 'DINNER' | 'SNACK';

export type MealItemRequest = {
  foodId: string | null;
  foodName: string;
  brand: string | null;
  quantityG: string;
  caloriesKcal: string;
  proteinG: string;
  carbsG: string;
  fatsG: string;
};

export type MealItemResponse = {
  id: string;
  foodId: string | null;
  foodName: string;
  brand: string | null;
  quantityG: string;
  caloriesKcal: string;
  proteinG: string;
  carbsG: string;
  fatsG: string;
};

export type MealDishResponse = {
  id: string;
  name: string;
  description: string | null;
  caloriesKcal: string;
  proteinG: string;
  carbsG: string;
  fatsG: string;
  mealItemIds: string[];
};

export type MealSlotResponse = {
  id: string;
  mealType: MealType;
  items: MealItemResponse[];
  dishes: MealDishResponse[];
};

export type NutritionDayRequest = {
  restingCaloriesKcal: number;
  activeCaloriesKcal: number;
  adjustmentCaloriesKcal: number;
  targetCaloriesKcal: number;
  targetProteinG: string;
  targetCarbsG: string;
  targetFatsG: string;
};

export type NutritionDayResponse = NutritionDayRequest & {
  id: string;
  day: string;
  mealSlots: MealSlotResponse[];
};

export type NutritionSummaryResponse = {
  day: string;
  caloriesKcal: string;
  proteinG: string;
  carbsG: string;
  fatsG: string;
  waterMl: number;
};
