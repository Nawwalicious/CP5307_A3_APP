# Puzzle Brain

An Android education app that teaches algebraic reasoning through picture
puzzles. Each puzzle shows a set of symbol equations; the learner works out what
the missing symbol is worth and picks the answer from four options.

Built with Kotlin and Jetpack Compose for CP5307 Assessment 3.

---

## Core features

| Feature | Where it lives | How it works |
| --- | --- | --- |
| Three difficulty levels | `assets/1`, `assets/2`, `assets/3` | Each folder is one level. Level 1 has a single unknown, level 2 introduces several symbols, level 3 requires solving a full system. |
| Randomised questions | `GameViewModel.startGame()` | Puzzles are shuffled per round when the setting is enabled, so replaying is not a memory test of the order. |
| Generated answer options | `logic/OptionGenerator.kt` | Only the correct answer is stored. The three distractors are derived from it as near misses and scale errors, so wrong options are plausible rather than random. Seeded, so options do not change on rotation. |
| Player slots (max 3) | `logic/GameRules.kt`, `screen/PlayerScreen.kt` | Creating a fourth player is blocked rather than overwriting an existing one. |
| Score history (max 10) | `AppDao.trimScores()` | The newest ten results are kept; older records are trimmed automatically after each insert. |
| Sound feedback | `helper/SoundPlayer.kt` | Short tones via `ToneGenerator`, so no audio assets or extra permissions are needed. Can be turned off. |
| Rotation safety | `viewmodel/GameViewModel.kt` | All game state lives in a ViewModel, so rotating the device preserves the question, score and generated options. |

## Architecture

```
com.example.eduapp
├── data/          PuzzleRepository, RemotePuzzleSource
├── database/      Room entities, DAO, DatabaseProvider
├── helper/        asset image loading, preferences, sound
├── logic/         GameRules, OptionGenerator, Puzzle (pure Kotlin)
├── screen/        Landing, Player, Level, Game, Score, Setting
├── ui/theme/      brand palette matched to the launcher icon
└── viewmodel/     AppViewModel, GameViewModel and their factories
```

The UI observes ViewModels; ViewModels depend on repositories and the DAO,
never on Android framework classes directly. Dependencies are supplied through
constructor injection using `ViewModelProvider.Factory`, and the Room database
is built once in `DatabaseProvider` rather than inside a composable.

Everything in `logic/` is pure Kotlin with no Android imports, which is what
makes the non-GUI unit tests possible.

## Screens

| Screen | Purpose |
| --- | --- |
| Landing | Entry point, main actions, and a plain statement of what data is stored. |
| Player | Choose, create or delete one of three player slots. |
| Level | Pick level 1, 2 or 3. |
| Game | The puzzle itself: image, four options, immediate feedback. |
| Score | The last ten results across all players, newest first. |
| Setting | Sound, shuffle, manage players, erase all data. |

Navigation uses the Navigation Component for Compose. Player name and level are
passed as typed route arguments.

## Database

Room, two tables:

- `players` — `id`, `name`, `createdAt`. Capped at three.
- `users` — `id`, `username`, `level`, `score`, `duration`, `date`. Capped at ten.

## Networking

`RemotePuzzleSource` fetches `puzzles.json` over HTTPS from the project's own
GitHub repository, so the answer key can be corrected without shipping a new
build. A complete copy is bundled in `assets/`, and the app falls back to it
whenever the network is unavailable, so the game works fully offline.

**Setup:** in `RemotePuzzleSource.DEFAULT_URL`, replace `USERNAME/REPO` with this
repository's path.

## Testing

Non-GUI unit tests in `app/src/test/java/com/example/eduapp/`:

- `GameRulesTest` — name validation (length, allowed characters, duplicates,
  trimming) and the three-slot limit.
- `OptionGeneratorTest` — always four options, always contains the correct
  answer, never duplicates, never negative, deterministic for a given seed.

Run with `./gradlew test`, or right-click the `test` source folder in Android
Studio. No device or emulator required.

## Ethical and professional design

- **Data minimisation.** No account, no email, no device identifier, no
  analytics. A player is a display name and nothing more.
- **User control.** Every stored item can be deleted from Settings, and each
  destructive action asks for confirmation and states what will be lost.
- **No dark patterns.** No timers, no streaks, no notifications, no pressure to
  return. The learner sets the pace.
- **Constructive feedback.** A wrong answer reveals the correct value rather
  than only marking the attempt as failed.
- **Accessibility.** Touch targets are at least 48dp; answer feedback is carried
  by text as well as colour; images and progress carry content descriptions;
  sound is optional and never the only signal.
- **Content safety.** All puzzle images ship with the app and are reviewed, so
  nothing unvetted can appear on screen.

## Build

- minSdk 26, targetSdk 36
- Kotlin, Jetpack Compose, Material 3
- Room 2.8.4 with KSP
- Navigation Compose 2.7.7
