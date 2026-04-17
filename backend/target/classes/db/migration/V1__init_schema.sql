CREATE TABLE "users" (
                         "id" uuid PRIMARY KEY,
                         "email" text UNIQUE,
                         "name" text NOT NULL,
                         "created_at" timestamptz NOT NULL DEFAULT (now()),
                         "updated_at" timestamptz NOT NULL DEFAULT (now())
);

CREATE TABLE "auth_identities" (
                                   "id" uuid PRIMARY KEY,
                                   "user_id" uuid NOT NULL,
                                   "provider" text NOT NULL,
                                   "provider_user_id" text NOT NULL,
                                   "created_at" timestamptz NOT NULL DEFAULT (now()),
                                   "updated_at" timestamptz NOT NULL DEFAULT (now())
);

CREATE TABLE "auth_sessions" (
                                 "id" uuid PRIMARY KEY,
                                 "user_id" uuid NOT NULL,
                                 "refresh_token_hash" text NOT NULL,
                                 "expires_at" timestamptz NOT NULL,
                                 "revoked_at" timestamptz,
                                 "created_at" timestamptz NOT NULL DEFAULT (now())
);

CREATE TABLE "weight_logs" (
                               "id" uuid PRIMARY KEY,
                               "user_id" uuid NOT NULL,
                               "weight_kg" numeric NOT NULL,
                               "recorded_at" timestamptz NOT NULL DEFAULT (now()),
                               "created_at" timestamptz NOT NULL DEFAULT (now())
);

CREATE TABLE "nutrition_profiles" (
                                      "id" uuid PRIMARY KEY,
                                      "user_id" uuid NOT NULL,
                                      "protein_percentage" numeric NOT NULL,
                                      "carbs_percentage" numeric NOT NULL,
                                      "fats_percentage" numeric NOT NULL,
                                      "created_at" timestamptz NOT NULL DEFAULT (now()),
                                      "updated_at" timestamptz NOT NULL DEFAULT (now())
);

CREATE TABLE "water_logs" (
                              "id" uuid PRIMARY KEY,
                              "user_id" uuid NOT NULL,
                              "amount_ml" int NOT NULL,
                              "recorded_at" timestamptz NOT NULL DEFAULT (now()),
                              "created_at" timestamptz NOT NULL DEFAULT (now())
);

CREATE TABLE "nutrition_days" (
                                  "id" uuid PRIMARY KEY,
                                  "user_id" uuid NOT NULL,
                                  "day" date NOT NULL,
                                  "resting_calories_kcal" int NOT NULL,
                                  "active_calories_kcal" int NOT NULL,
                                  "adjustment_calories_kcal" int NOT NULL DEFAULT 0,
                                  "target_calories_kcal" int NOT NULL,
                                  "target_protein_g" numeric NOT NULL,
                                  "target_carbs_g" numeric NOT NULL,
                                  "target_fats_g" numeric NOT NULL,
                                  "created_at" timestamptz NOT NULL DEFAULT (now()),
                                  "updated_at" timestamptz NOT NULL DEFAULT (now())
);

CREATE TABLE "meal_slots" (
                              "id" uuid PRIMARY KEY,
                              "nutrition_day_id" uuid NOT NULL,
                              "meal_type" text NOT NULL,
                              "created_at" timestamptz NOT NULL DEFAULT (now())
);

CREATE TABLE "foods" (
                         "id" uuid PRIMARY KEY,
                         "source" text NOT NULL,
                         "source_id" text,
                         "barcode" text,
                         "name" text NOT NULL,
                         "brand" text,
                         "calories_per_100g" numeric NOT NULL,
                         "protein_per_100g" numeric NOT NULL,
                         "carbs_per_100g" numeric NOT NULL,
                         "fat_per_100g" numeric NOT NULL,
                         "created_at" timestamptz NOT NULL DEFAULT (now()),
                         "updated_at" timestamptz NOT NULL DEFAULT (now())
);

CREATE TABLE "meal_items" (
                              "id" uuid PRIMARY KEY,
                              "meal_slot_id" uuid NOT NULL,
                              "food_id" uuid,
                              "food_name" text NOT NULL,
                              "brand" text,
                              "quantity_g" numeric NOT NULL,
                              "calories_kcal" numeric NOT NULL,
                              "protein_g" numeric NOT NULL,
                              "carbs_g" numeric NOT NULL,
                              "fats_g" numeric NOT NULL,
                              "created_at" timestamptz NOT NULL DEFAULT (now())
);

CREATE TABLE "food_search_history" (
                                       "id" uuid PRIMARY KEY,
                                       "user_id" uuid NOT NULL,
                                       "search_text" text NOT NULL,
                                       "created_at" timestamptz NOT NULL DEFAULT (now())
);

CREATE TABLE "meal_dishes" (
                               "id" uuid PRIMARY KEY,
                               "meal_slot_id" uuid NOT NULL,
                               "name" text NOT NULL,
                               "description" text,
                               "calories_kcal" numeric NOT NULL,
                               "protein_g" numeric NOT NULL,
                               "carbs_g" numeric NOT NULL,
                               "fats_g" numeric NOT NULL,
                               "created_at" timestamptz NOT NULL DEFAULT (now()),
                               "updated_at" timestamptz NOT NULL DEFAULT (now())
);

CREATE TABLE "meal_dishes_items" (
                                     "id" uuid PRIMARY KEY,
                                     "meal_dish_id" uuid NOT NULL,
                                     "meal_item_id" uuid NOT NULL,
                                     "created_at" timestamptz NOT NULL DEFAULT (now())
);

CREATE INDEX ON "users" ("email");

CREATE UNIQUE INDEX ON "auth_identities" ("provider", "provider_user_id");

CREATE INDEX ON "auth_identities" ("user_id");

CREATE INDEX ON "auth_sessions" ("user_id");

CREATE UNIQUE INDEX ON "auth_sessions" ("refresh_token_hash");

CREATE INDEX ON "weight_logs" ("user_id", "recorded_at");

CREATE UNIQUE INDEX ON "nutrition_profiles" ("user_id");

CREATE INDEX ON "water_logs" ("user_id");

CREATE INDEX ON "water_logs" ("recorded_at");

CREATE UNIQUE INDEX ON "nutrition_days" ("user_id", "day");

CREATE INDEX ON "nutrition_days" ("user_id");

CREATE UNIQUE INDEX ON "meal_slots" ("nutrition_day_id", "meal_type");

CREATE INDEX ON "meal_slots" ("nutrition_day_id");

CREATE UNIQUE INDEX ON "foods" ("source", "source_id");

CREATE INDEX ON "foods" ("barcode");

CREATE INDEX ON "foods" ("name");

CREATE INDEX ON "meal_items" ("meal_slot_id");

CREATE INDEX ON "meal_items" ("food_id");

CREATE INDEX ON "food_search_history" ("user_id");

CREATE INDEX ON "food_search_history" ("created_at");

CREATE INDEX ON "meal_dishes" ("meal_slot_id");

CREATE UNIQUE INDEX ON "meal_dishes_items" ("meal_dish_id", "meal_item_id");

CREATE INDEX ON "meal_dishes_items" ("meal_dish_id");

CREATE INDEX ON "meal_dishes_items" ("meal_item_id");

ALTER TABLE "auth_identities" ADD FOREIGN KEY ("user_id") REFERENCES "users" ("id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "auth_sessions" ADD FOREIGN KEY ("user_id") REFERENCES "users" ("id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "weight_logs" ADD FOREIGN KEY ("user_id") REFERENCES "users" ("id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "nutrition_profiles" ADD FOREIGN KEY ("user_id") REFERENCES "users" ("id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "water_logs" ADD FOREIGN KEY ("user_id") REFERENCES "users" ("id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "nutrition_days" ADD FOREIGN KEY ("user_id") REFERENCES "users" ("id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "meal_slots" ADD FOREIGN KEY ("nutrition_day_id") REFERENCES "nutrition_days" ("id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "meal_items" ADD FOREIGN KEY ("meal_slot_id") REFERENCES "meal_slots" ("id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "meal_items" ADD FOREIGN KEY ("food_id") REFERENCES "foods" ("id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "food_search_history" ADD FOREIGN KEY ("user_id") REFERENCES "users" ("id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "meal_dishes" ADD FOREIGN KEY ("meal_slot_id") REFERENCES "meal_slots" ("id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "meal_dishes_items" ADD FOREIGN KEY ("meal_dish_id") REFERENCES "meal_dishes" ("id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "meal_dishes_items" ADD FOREIGN KEY ("meal_item_id") REFERENCES "meal_items" ("id") DEFERRABLE INITIALLY IMMEDIATE;
