# ReNote — Technical Specification (v1)

Single source of truth for implementation. Derived from the five design documents
(`General idea`, `Daily view`, `Weekly view`, `Month view`, `Item detail screen v1`)
plus the sketch mockups. Where a document and a mockup disagree, this file wins.

---

## 1. Product summary

A personal note/schedule app. Notes are attached to a calendar day and optionally to a
time of day. Three ways to look at them — Day, Week, Month — plus a detail screen for
creating and editing a note.

Out of scope for v1: push notifications and reminders, event duration (start time only),
recurring notes, categories/tags, search, attachments, sync, multi-user, completion state.

---

## 2. Technical stack

- Kotlin, Jetpack Compose, Material 3
- Room (KSP) for persistence
- ViewModel + StateFlow, unidirectional data flow
- Navigation Compose, single Activity
- `java.time` (minSdk 26, so no desugaring needed)
- No DI framework in v1 — manual construction via a small `AppContainer`

---

## 3. Data model

```kotlin
@Entity(
    tableName = "notes",
    indices = [Index("date")]
)
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,              // 1..200 chars, mandatory
    val description: String = "",   // unlimited, optional
    val date: LocalDate,            // the day this note belongs to
    val time: LocalTime? = null,    // optional start time
    val createdAt: Instant,
    val updatedAt: Instant
)
```

`date` is the only link between a note and a view. It is set when the note is created
(from the day currently shown in the Daily view) and **cannot be changed afterwards**.

TypeConverters: `LocalDate` ↔ epoch day (`Long`), `LocalTime` ↔ seconds of day
(`Int?`), `Instant` ↔ epoch milli (`Long`).

### DAO

```kotlin
@Query("""
    SELECT * FROM notes
    WHERE date = :date
    ORDER BY (time IS NULL), time ASC, createdAt ASC
""")
fun observeByDate(date: LocalDate): Flow<List<Note>>

@Query("SELECT * FROM notes WHERE date BETWEEN :from AND :to")
fun observeByDateRange(from: LocalDate, to: LocalDate): Flow<List<Note>>

@Query("SELECT DISTINCT date FROM notes WHERE date BETWEEN :from AND :to")
fun observeDatesWithNotes(from: LocalDate, to: LocalDate): Flow<List<LocalDate>>

suspend fun insert(note: Note): Long
suspend fun update(note: Note)
suspend fun delete(note: Note)
suspend fun getById(id: Long): Note?
```

**Sort order** (used everywhere a day's notes are listed): notes with a time first,
ascending by time; then notes without a time, ascending by `createdAt`.
`(time IS NULL)` evaluates to 0/1 in SQLite, which produces exactly that.

The month view must not load every note of the month — only `observeDatesWithNotes`.

---

## 4. Calendar conventions

- **The week starts on Monday.** Hard-coded (`DayOfWeek.MONDAY`), not taken from the
  locale, so the layout is identical on every device.
- Dates and times are rendered with the system format (`DateFormat.getBestDateTimePattern`
  / `DateTimeFormatter.ofLocalizedDate`), so a 12/24h clock follows device settings.
- All dates are local dates with no timezone attached. Nothing is stored in UTC.

---

## 5. Navigation

Routes:

```
daily/{date}?from={none|week|month}      // date = ISO yyyy-MM-dd
weekly/{anchorDate}                       // any date inside the target week
monthly/{yearMonth}                       // yyyy-MM
detail/new?date={date}
detail/{noteId}
```

Start destination: `daily/<today>?from=none`.

### The D/W/M switcher

A segmented control in the top-right corner of Daily, Weekly and Monthly.

It **replaces** the current screen rather than stacking on top of it, and it does not
contribute to the back stack. Implementation: `popUpTo` the current destination with
`inclusive = true`.

Target of each button (v1, taken from `Month view` and left as written until live
testing says otherwise):

- `D` → the **current** day
- `W` → the **current** week
- `M` → the month currently being viewed. From Daily, the month of the shown day. From
  Weekly, where a week can span two months: today's month if today is in the shown week,
  otherwise the month of the week's Thursday (the one holding most of its days).

> Open point, to revisit after live testing: carrying the viewed date across the switch
> (viewing 20 March → `W` → week of 16–22 March) is more intuitive than always jumping
> back to today. Deliberately deferred, not forgotten.

### The "<" back button

Shown in the top-left corner only when the screen was reached by **drilling down**, i.e.
`from != none`:

| Path to Daily | `from` | "<" shown |
|---|---|---|
| App start | `none` | no |
| D/W/M switcher | `none` | no |
| Tap on a day tile in Weekly | `week` | yes, returns to that week |
| Tap on a day in Monthly | `month` | yes, returns to that month |

The system back button behaves identically to "<" wherever "<" is shown.

---

## 6. Screens

### 6.1 Daily view (start screen)

- Title: the date of the day being shown, in system format. Today by default.
- Top right: D/W/M switcher. Top left: "<" per the rule above.
- Body: a list of that day's notes in the sort order of §3.
  - Note row shows the title and, when present, the time.
  - Title is limited to 2 lines, overflowing with an ellipsis.
  - Row height target: **~88dp**, so about 7 rows fit on a typical phone screen without
    scrolling. This is a guideline for sizing, not a hard rule — fewer rows on a small
    screen is acceptable. The list scrolls freely beyond that.
- Empty state: a centred hint, "There are no added items in the list yet. Click "+" to
  add.", with the FAB still present. It is hidden as soon as the day has at least one note,
  and is not shown while the day's notes are still loading.
- **FAB "+"**: bottom right, present on Daily **only**. It does not overlap content
  (`Scaffold` content padding + a trailing spacer in the list). It opens
  `detail/new?date=<the day currently shown>` — the shown day, never today.
- **Horizontal swipe** moves to the previous / next day. Implemented with
  `HorizontalPager` over an effectively unbounded page range centred on the start date,
  so this must be built into the screen from the start rather than added later.
- Tapping a note opens `detail/{id}`.

### 6.2 Weekly view

- Title: the week's date range in system format. Top right: D/W/M switcher.
- **No FAB.** (The mockup shows one; the requirements say "+" is Daily-only and win.)
- Layout: a grid filling the screen exactly, with no outer scroll —
  **2 columns × 3 rows** for Monday–Saturday, then **Sunday full width** in the last row.
  Keeping the outer surface non-scrollable is what lets each tile scroll internally
  without a nested-scroll conflict.
- Each tile: the day name as a header, then that day's notes in the standard sort order,
  as a compact version of the Daily row (title only, one line, ellipsised).
  - Up to 5 items are listed; if the day has 6 or more, the tile's own list scrolls and
    shows a scroll indicator.
  - An empty day shows an empty tile.
- The current day's tile has a visibly thicker border.
- Tapping a tile opens `daily/{thatDate}?from=week`.
- **Horizontal swipe** moves to the previous / next week.

### 6.3 Month view

- Title: month name and full year, e.g. "October 2026".
- Top right: D/W/M switcher. No back button (the month is only reachable via the switcher).
- A standard calendar grid: 7 columns starting Monday, with a weekday header row, and
  **always 6 rows** so the grid height does not change between months. Days belonging to
  the neighbouring months fill the leading and trailing cells, dimmed.
- A day with at least one note is marked with a dot below the number.
- The current day is visually distinguished (filled circle behind the number).
- Tapping any day opens `daily/{thatDate}?from=month`.
- **Horizontal swipe** moves to the previous / next month.

### 6.4 Item detail screen

Used both for creating (`detail/new?date=…`) and editing (`detail/{id}`).

Fields:

- **Title** — single-line text field, mandatory. Hard limit of 200 characters: input
  beyond 200 is **rejected at the field**, not flagged afterwards (the `onValueChange`
  callback ignores longer values). A character counter is shown near the limit.
- **Description** — multi-line text field, optional, no limit.
- **Time** — optional and absent by default. A "+ Add time" affordance opens the native
  `TimePickerDialog` in the system's own 12/24h format. Once a time is set, it is shown
  in place of the affordance with a small clear control that removes it again.
- The **date is not editable** here. It comes from the Daily view day the screen was
  opened from and is not displayed as a control.

Actions:

- **Save** — validates the title is non-blank, writes the note, navigates back to
  `daily/{date}`. Disabled while the title is blank.
- **Cancel** — discards everything and navigates back to `daily/{date}`.
  The system back button is equivalent to Cancel.
- **Delete** — only on an existing note. A red "x" in the top-right corner opens a
  confirmation dialog reading "Are you sure you want to delete the item?", with:
  - "Yes" → the note is deleted and the user returns to `daily/{date}`
  - "No" → the dialog closes, the user stays on the detail screen

> The mockup draws a red trash-can icon here instead of an "x". The requirements say "x",
> so "x" it is — but a trash icon is the more conventional Android affordance and is
> worth reconsidering.

---

## 7. Visual direction

The mockups establish a paper-sketch aesthetic: cream/off-white background, handwritten
lettering, pencil-drawn borders, a muted amber accent for the selected switcher segment
and the FAB.

v1 approximation (full confirmation pending):

- Cream surface (`#FAF6EC`-ish), dark warm grey text, amber accent.
- A handwriting font from Google Fonts for titles and day names; a readable sans for note
  bodies and input fields.
- Standard Material 3 borders and shapes. Genuine hand-drawn borders would mean custom
  `Canvas` drawing or nine-patch assets and are out of scope for v1.
- Light theme only in v1.

---

## 8. Open points requiring confirmation

1. **The "✓" marks in the weekly mockup's tiles.** Treated here as decoration with no
   meaning — v1 has no completion state. If notes should be completable, the model needs
   an `isDone` flag and the UI needs checkboxes across all three views.
2. **Clearing a time once set** — assumed to be a small clear control next to the time.
   Not specified anywhere in the source documents.
3. **Cancel with unsaved edits** — assumed to discard silently, per the requirements. A
   "discard changes?" confirmation is the common alternative.
4. **D/W/M target** — see §5; kept as "current day / current week" until live testing.
5. **Depth of the sketch aesthetic** — see §7.

---

## 9. Build order

Each step ends with something runnable on an emulator. Commit after each one.

1. **Data layer.** Entity, converters, DAO, database, Room dependencies. Verify with an
   instrumented DAO test covering the sort order.
2. **Daily view, static.** Full layout including the `HorizontalPager` day swipe, driven
   by a hard-coded list. No Room yet.
3. **Item detail, static.** All fields, the 200-char block, the time picker, the delete
   dialog. No persistence yet.
4. **Wire them together.** Navigation Daily ↔ Detail, ViewModels, real Room. At the end
   of this step the app genuinely works for a single day.
5. **Weekly view.** Grid, tile internal scrolling, thicker border on today, week swipe,
   drill-down to Daily with "<".
6. **Month view.** Calendar grid, dots, month swipe, drill-down to Daily with "<".
7. **D/W/M switcher.** Wire all three screens together and settle the back-stack rules.
8. **Theming pass.** Apply §7 across every screen.
