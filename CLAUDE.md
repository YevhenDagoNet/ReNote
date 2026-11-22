# ReNote

A personal note/schedule app for Android. Notes belong to a calendar day and optionally
to a time of day, and can be viewed by day, week or month.

**`docs/spec.md` is the authoritative specification.** Read it before implementing any
screen or data change, and follow it over the original PDFs in `docs/`, which are the
earlier source material and contain known contradictions.

If something is genuinely unspecified, ask rather than inventing behaviour.

## Stack

- Kotlin, Jetpack Compose, Material 3
- Room with KSP
- ViewModel + StateFlow, unidirectional data flow
- Navigation Compose, single Activity
- `java.time`, minSdk 26
- No DI framework — manual wiring through a small `AppContainer`

## Structure

```
app/src/main/java/com/example/renote/
  data/        Room: Note entity, Converters, NoteDao, NoteDatabase, NoteRepository
  ui/daily/    DailyScreen, DailyViewModel
  ui/weekly/   WeeklyScreen, WeeklyViewModel
  ui/monthly/  MonthlyScreen, MonthlyViewModel
  ui/detail/   DetailScreen, DetailViewModel
  ui/common/   shared composables: ViewSwitcher, NoteRow, ConfirmDialog
  ui/theme/    Color, Type, Theme
  navigation/  NavGraph, routes
```

## Conventions

- Composables are stateless: state comes in as parameters, events go out as lambdas.
  ViewModels are collected only at screen-level composables, never inside leaf ones.
- One `UiState` data class per screen, exposed as a single `StateFlow`.
- All business logic lives in the ViewModel or the repository, never in a composable.
- Room returns `Flow`; nothing in the UI layer queries the database directly.
- The week starts on Monday, hard-coded. Never derive it from the locale.
- Dates and times are formatted with system patterns so 12/24h follows device settings.
- Day-note ordering is always: timed notes by time, then untimed notes by `createdAt`.
  This lives in the DAO query — do not re-sort in the UI.
- New strings go in `strings.xml`, never hard-coded in composables.

## Workflow

- Build with `./gradlew assembleDebug` after making changes and fix what breaks.
- Work one step at a time, following the build order in `docs/spec.md` §9.
- Prefer small, reviewable changes over large sweeping ones.
