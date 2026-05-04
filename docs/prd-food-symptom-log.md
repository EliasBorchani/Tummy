# PRD — Food & Symptom Log (MVP)

## 1. User & job-to-be-done

Someone who suspects a food intolerance and wants to find the culprit without
a dietitian.

## 2. Outcome

A transparent per-ingredient *suspect score* computed from daily logs of
ingredients eaten and binary symptoms experienced. The user sees, next to
each ingredient, a colored dot reflecting how strongly that ingredient
correlates with their worst symptom.

## 3. Scope

### In scope (MVP)

- Per-day logging of ingredients (curated list with autocomplete + custom
  free-text entries).
- Per-day logging of symptoms from a fixed list of 6, binary on/off.
- Day view with horizontal navigation between days.
- Color-dotted suspect score next to each ingredient.
- Edit any past day, anytime.
- English only.
- 100% native UI (SwiftUI on iOS, Compose on Android).

### Out of scope

- Per-meal / half-day granularity.
- Photos, barcode scan, nutrition info.
- Multi-user / accounts.
- Export, sharing, sync, backend.
- Reminder notifications.
- Symptom severity grading.
- Dedicated insights / suspects tab.
- Dev-curated promotion loop for custom entries (no upload path exists at
  MVP, so custom entries stay local).
- Localization beyond English.
- Decomposable composite foods ("pizza" → wheat + cheese + …). Curated list
  is single ingredients only at MVP.

## 4. Data model

Three Room tables, no parent row. Day grouping is done by `date` in queries;
edits are insert/delete on a `(date, …)` key.

```
IngredientLog(date, curatedId)
CustomIngredientLog(date, name)   // name normalized: lowercase + trim + collapse internal whitespace
SymptomLog(date, symptom)         // symptom = enum
```

- Curated ingredients are not a Room table. They ship as static
  `CuratedIngredient(id, nameKey: StringResource)` declared in code, with
  display names in MOKO. The id is what gets persisted.
- Per-day dedupe: an insert is silently ignored if the same `curatedId` or
  the same normalized `name` is already logged for that date.
- Before saving a typed string as a new `CustomIngredientLog`, fuzzy-match
  it against curated names — if a curated match passes the threshold and
  the user accepts it, log as `IngredientLog` instead. Prevents
  curated/custom duplicates of the same thing.

## 5. Symptoms

Fixed enum, 6 values:

```
bloating, abdominal_pain, gas, diarrhea, constipation, nausea
```

Binary on/off per day. Tap once = on, tap again = off. No severity, no
count.

## 6. Scoring

Computed dynamically by a use case when a view opens. Never persisted.

### Per (ingredient, symptom) score

```
suspect_score(I, S) = P(S | ate I) − P(S | didn't eat I)
```

Range ≈ −1…+1. Time-lag weighted: a symptom logged on the same day as the
ingredient counts with weight 1.0; a symptom logged the next day counts
with weight 0.5.

### Dot color is driven by the worst symptom

```
displayed_score(I) = max over S of suspect_score(I, S)
```

Tap the dot → popover lists the top 1–2 symptoms with their raw counts,
e.g. *"bloating after 4/5 times you ate it; gas after 3/5 times."*

### Color thresholds

- **Grey** — insufficient data (see thresholds below).
- **Green** — `displayed_score(I) ≤ 0` (no signal).
- **Yellow** — `0 < displayed_score(I) ≤ 0.3` (weak suspect).
- **Red** — `displayed_score(I) > 0.3` (strong suspect).

### Minimum data thresholds

Below threshold, the dot is grey and an empty-state hint is shown
("N more days of logging to start seeing patterns").

- Global: ≥ 7 logged days total.
- Per ingredient: appears on ≥ 3 days *and* ≥ 3 days exist where it wasn't
  eaten.

## 7. Autocomplete

Cheap fuzzy match (character bigrams + Levenshtein). Search runs over:

1. Localized curated ingredient names.
2. Past `CustomIngredientLog` entries, deduplicated by normalized name.

If no suggestion is accepted, the typed string is saved as a new
`CustomIngredientLog`. Curated matches always win over custom matches at
the same fuzzy distance.

## 8. UI flow

### Day view (home)

- Paginated by date. Horizontal scroll *and* left/right arrow buttons to
  navigate.
- Lists ingredients (with their suspect dot on the right) and symptoms
  logged for that date.
- Empty day state: empty list + an encouraging line of copy.
- Swipe-to-delete on each row (`SwipeActions` in SwiftUI,
  `SwipeToDismissBox` in Compose).

### Add flow

- Entry point per platform idiom:
  - **iOS** — trailing `+` button in the navigation bar (no FAB; not
    native to iOS).
  - **Android** — Material FAB.
- Tap → action sheet / bottom sheet with two options: *Add ingredient* /
  *Add symptom*. Each leads to its own screen.

### Add ingredient screen

- Search input → autocomplete suggestions (curated + past custom).
- Selecting a curated suggestion → `IngredientLog` insert.
- Selecting a past custom suggestion or typing a new one → matched against
  curated first; otherwise `CustomIngredientLog` insert.
- Returns to day view on save.

### Add symptom screen

- List of the 6 symptoms with toggle behavior on the current day.

## 9. Architecture & DI

Two independent bounded contexts, plus two feature modules. Per the
project's principle that domain modules never depend on each other,
cross-context composition (the suspect-score computation) lives in the
home VM.

```
domain/ingredients/api       // CuratedIngredient, IngredientRepository,
                             //   SearchIngredientsUseCase (fuzzy autocomplete)
domain/ingredients/data      // Room: IngredientLog, CustomIngredientLog,
                             //   IngredientsDatabase, ingredientsModule
domain/symptoms/api          // Symptom enum, SymptomRepository
domain/symptoms/data         // Room: SymptomLog, SymptomsDatabase, symptomsModule

features/home                // HomeViewModel: day state, day navigation,
                             //   computes suspect_score(I, S) inline by
                             //   injecting IngredientRepository + SymptomRepository
features/log                 // LogIngredientViewModel, LogSymptomViewModel
```

- Each domain context owns its own Room `@Database` (per project
  convention — no shared mega-DB).
- DI registration: both `ingredientsModule` and `symptomsModule` are wired
  in `umbrella/TummyKoin.kt` (iOS) and `composeApp/.../di/AppModule.kt`
  (Android). VMs are registered in both files following the project's
  feature-module shape.
- All data is local. No backend, no sync.
