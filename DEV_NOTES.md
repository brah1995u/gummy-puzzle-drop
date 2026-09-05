# Gummy Puzzle Drop

A portrait Android puzzle game where four-cell gummy pieces fall as rigid shapes, lock into
independent candies, and clear only by horizontal or vertical Match-3. Clears trigger per-column
gravity, cascades, combo scoring, and a special Pink Bomb reward at combo ×3.

## Controls

- Drag left/right: move by board columns.
- Tap: rotate clockwise with wall kicks.
- Fast swipe down: hard drop and lock.
- HOLD: store/swap one exact normal piece per turn.

## Rules at a glance

- Internal board is 8 × 16 (14 visible + 2 hidden spawn rows).
- Seven generic four-cell shapes use a shuffled 7-bag.
- Rows do not clear. Only 3+ equal normal candies in horizontal/vertical runs clear.
- Locked candies fall independently after clears.
- Combo ×3 awards a playable one-cell Pink Bomb that clears a clipped 3 × 3 area.
- Level increases every 10 locked normal pieces; blocked spawn after resolution ends the game.

## Offline progress

- The submission-ready main menu uses a glossy gummy mascot showcase, illustrated action buttons,
  reusable candy-art frames, and live badges for daily, achievement, shop, leaderboard, and Blitz status.
- Ten permanent achievements expose exact progress and unlock automatically.
- Three Daily Sugar Rush goals rotate at local midnight and build a completion streak.
- The Local Leaderboard keeps the best 10 completed runs with score, level, combo, and date.
- Sugar Stars are earned from completed runs, achievements, and full daily sets.
- The offline Sugar Shop unlocks cosmetic board themes and drop effects; purchases never alter gameplay.
- Power Treats are consumable gameplay bonuses bought with earned stars: Pink Bomb, Rainbow Pop,
  and Sweet Cleanup. They are activated directly from the gameplay HUD.
- The live board, Hold, and Next previews use separate non-stretched candy frames derived from
  the supplied UI sheet, while the full 8×14 play area and gestures remain intact.
- Gummy Blitz is a deterministic 20-second tap mini-game with combo, three lives, a daily Sugar
  Star reward, and score-tier Power Treat prizes.
- No account, backend, network permission, real-money purchase, ads, or unapproved boosters are used.

## Settings

Music, sound effects, haptics, landing ghost, and reduced motion are independent candy-style
toggles and persist across launches. The tutorial can be replayed at any time.

## Architecture

The `game` packages are a deterministic, Android-free Kotlin engine. Android presentation owns
frame timing, DataStore persistence, feedback, lifecycle pause, and Compose UI. See
[`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) and [`docs/MASTER_SPEC.md`](docs/MASTER_SPEC.md).

## Build

Requirements: JDK 17 and Android SDK 35.

```powershell
./gradlew.bat :app:assembleDebug
```

## Test

```powershell
./gradlew.bat :app:testDebugUnitTest
```

The current suite contains 53 deterministic engine/meta/shop/mini-game tests. Android Lint, debug assembly, and the
unsigned release assembly are available through:

```powershell
./gradlew.bat :app:lintDebug :app:assembleDebug :app:assembleRelease
```

## Assets

Supplied designs are preserved under `design/originals/`. Production gummies are individual
transparent assets. Glossy button/panel rims use non-stretching three-/nine-slice rendering while
simple fills, toggle geometry, gradients, and effects remain Compose-drawn. See
[`docs/ASSET_MANIFEST.md`](docs/ASSET_MANIFEST.md).

## Central tuning

Board dimensions, progression thresholds, speed table, lock delay, wall kicks, danger rows,
score table, and bomb radius live in `GameConfig`.
