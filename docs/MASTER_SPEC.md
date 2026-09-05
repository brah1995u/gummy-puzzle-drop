# Gummy Puzzle Drop — Master Specification

This document is the local product source of truth. It distills the approved rules from the
user-supplied autonomous development master specification without changing gameplay.

## Product

`Gummy Puzzle Drop` is a portrait Android casual puzzle game combining falling four-cell
shapes, orthogonal gummy Match-3, independent-cell gravity, cascades, and combo scoring.

Turn flow:

`SPAWN → MOVE/ROTATE → DROP → LOCK → MATCH → CLEAR → GRAVITY → CASCADE → SCORE → NEXT`

The session ends only when the next required piece cannot legally spawn after resolution.
This is not classic row-clearing gameplay: filled rows never clear by themselves.

## Board and coordinates

- 8 columns.
- 14 visible rows plus 2 hidden spawn rows.
- Internal board size: 8 × 16.
- Coordinates use column `x` from left to right and row `y` from top to bottom.
- All dimensions and tuning values live in `GameConfig`.

## Pieces and generation

- Normal pieces use the seven generic four-cell shapes: I, O, T, L, J, S, Z.
- Shape order uses a true shuffled 7-bag. Every shape occurs once per bag.
- Rotation has explicit 0°, 90°, 180°, 270° states.
- Each piece carries exactly four immutable candy values that rotate with its cells.
- A normal piece may not contain more than two candies of any one type.
- Level 1–3 pool: Green Bear, Purple Bear, Red Bear.
- Level 4–6 adds Green Star.
- Level 7+ adds Orange Heart.
- After lock, the piece ceases to exist and its four candies become independent board cells.

## Controls and falling

- Horizontal drag moves in grid columns.
- A short tap rotates clockwise once.
- A fast downward swipe hard-drops to the exact ghost position and locks immediately.
- Automatic falling uses frame delta time and level fall interval, never a delay loop.
- Natural landing has a configurable 400 ms lock delay; legal movement/rotation resets it.
- Rotation wall-kick candidates are tried in order: 0, -1, +1, -2, +2.
- The ghost uses the same collision logic as hard drop.

## Matching, gravity, and cascades

- Match only 3+ identical normal candies horizontally or vertically.
- Diagonals do not count.
- The Pink Bomb is special and does not participate in normal matches.
- Scan full horizontal and vertical runs, union matched positions, then remove unique cells.
- Crossing matches remove and score their shared center once.
- Gravity compresses each column downward while preserving survivor order.
- Match/clear/gravity repeats until stable before another player-controlled piece spawns.
- First clear is combo ×1, first gravity-created clear ×2, then ×3, and so on.
- Combo resets when the board stabilizes and the next normal turn starts.

## Score

| Unique gummies removed | Base score |
|---:|---:|
| 3 | 30 |
| 4 | 60 |
| 5 | 100 |
| 6 | 150 |
| 7 | 200 |

Each candy beyond 7 adds 50. Multiply the base by the cascade combo. Hard drop adds one
point per vertically skipped cell.

## Pink Bomb

- Reaching combo ×3 awards one Pink Bomb after the current cascade fully finishes.
- The reward is a deterministic single-cell playable special piece.
- Hold is disabled for special pieces.
- On landing, the bomb destroys a clipped 3 × 3 area centered on itself.
- Explosion is followed by gravity, match detection, and any resulting cascades.
- A cascade can earn at most the approved pending bomb reward for that resolution chain.

## Next and Hold

- NEXT shows exact shape and exact four-candy composition.
- HOLD stores exact shape/composition, restores default rotation, and never rerolls candies.
- First hold stores current and consumes Next.
- Hold may be used once per active normal piece and resets after a normal lock.
- Pink Bomb cannot be held.

## Level and speed

- Level rises every 10 successfully placed normal pieces; score does not set level.

| Level | Fall interval |
|---:|---:|
| 1 | 900 ms |
| 2 | 780 ms |
| 3 | 680 ms |
| 4 | 590 ms |
| 5 | 510 ms |
| 6 | 440 ms |
| 7 | 380 ms |
| 8 | 330 ms |
| 9 | 290 ms |
| 10+ | 250 ms |

## State and game over

The domain uses explicit phases: SPAWNING, FALLING, LOCK_DELAY, RESOLVING, CASCADE,
PAUSED, and GAME_OVER. Danger feedback activates when cells enter roughly the top three
visible rows but never changes gameplay. Game over occurs only when the next piece has no
legal spawn after board resolution.

## Required V1 screens

- Short branded Splash with no artificial delay.
- Main Menu: logo/title, Play, Best Score, streak, Daily, Achievements, Local Leaderboard,
  and Settings.
- Gameplay: Score, Level, Next, Hold, Pause, dominant board, combo feedback.
- Pause: Resume, Restart, Home.
- Settings: Music, Sound Effects, Haptics, Ghost Piece, Reduce Motion, and Replay Tutorial.
- Achievements: permanent milestone list with visible progress and unlock state.
- Daily Sugar Rush: three deterministic daily goals, local-midnight reset, completion state,
  and consecutive-day streak.
- Local Leaderboard: best 10 completed runs on the current device; no account or network.
- Sugar Shop: earned Sugar Stars, cosmetic board themes and drop effects, purchase/equip states.
- Gameplay Power Treats: purchased consumable Pink Bomb, Rainbow Pop, and Sweet Cleanup actions,
  activated from the live gameplay HUD and resolved deterministically by the pure Kotlin engine.
- Gummy Blitz: 20-second 3×3 target-tapping mini-game with combo, three misses, best score,
  one daily reward, and practice replays.
- Run Result: dedicated illustrated daily victory and game-over presentations with Score, Best Score,
  Max Combo, Sugar Stars earned, Play Again, and Home.
- One concise first-run tutorial teaches drag, tap, swipe down, and Match-3; completion persists.

## Approved offline meta systems

The 2026-08-20 user extensions add achievements, daily goals, a leaderboard, and a cosmetic shop.
These systems remain deliberately offline and do not introduce real-money purchases,
authentication, or a backend. Sugar Stars are earned local currency awarded
after every completed run, with bonuses for newly unlocked achievements and a completed daily set.
Purchased board themes and drop effects can be equipped and appear in gameplay without changing
collision, matching, score, speed, or any other engine rule. The later explicit user extension
adds purchased consumable Power Treats; unlike cosmetics, these intentionally invoke isolated,
tested engine actions and decrement only when an effect is successfully applied. Gummy Blitz is
also explicitly approved as a local mini-mode. Achievement progress is derived from durable lifetime statistics. Daily goals rotate
from a deterministic six-goal pool, reset at the device's local midnight, complete automatically,
and award only a visible streak. The leaderboard stores the score, level, max combo, and date for
the best 10 completed local runs.

## Technology and architecture

- Native Android, Kotlin, Jetpack Compose, Compose Canvas.
- Pure Kotlin core: no Android, Context, Activity, Compose, Canvas, Drawable, DataStore,
  SoundPool, or Android ViewModel dependencies.
- Engine transitions are deterministic and animation-independent.
- Presentation may animate domain events, but animation delays never determine correctness.
- DataStore persists lifetime statistics, daily state/streak, a top-10 local leaderboard, earned
  Sugar Stars, owned/equipped cosmetics, Power Treat inventory, Gummy Blitz best/daily reward,
  tutorial seen, and settings.
- Gameplay pauses when the app backgrounds and stays paused on return.

## Visual and feel

Follow the supplied design: glossy translucent gummy characters, saturated candy palette,
pink/purple rounded panels, playful highlights, soft depth, and readable contrast. Avoid a
generic Material look. The board remains dominant and responsive across 16:9 through 20:9.
Gummies keep their aspect ratio.

Expected feel: 80–100 ms horizontal interpolation, ~100 ms rotation transition, subtle landing
squash, match pop, visible gravity, concise combo callout, and bomb particles/shake/haptic.

## Assets

- Supplied source designs remain read-only.
- Final gameplay gummies are individual transparent production assets.
- Do not ship a runtime sprite sheet, dirty crops, crop borders, stock art, emoji, or stretched art.
- Draw simple panels, borders, gradients, glows, and particles in Compose when equal or better.
- Recreate an asset if clean extraction is impossible.
- Track every production asset in `ASSET_MANIFEST.md`.

## Audio and haptics

Provide move, rotate, hard-drop, land, pop, combo, bomb, button, and game-over feedback,
plus light candy ambience/music. Respect settings and lifecycle. Do not vibrate on every
horizontal step.

## Required verification

Unit-test 7-bag generation, candy constraints and pools, collisions, rotations/wall kicks,
matches, gravity/order, ×2/×3 cascades, scoring, bomb center/edge/corner and aftermath,
hold rules/composition, and blocked spawn/game over. Build and run representative portrait,
lifecycle, restart, pause, and game-over QA before completion.
Also unit-test daily rotation/reset/streak, achievement derivation, aggregate statistics,
leaderboard sorting/capping, shop purchase safety, ownership, equipping, and run rewards.

## Non-goals

No campaign, level map, boosters beyond the three approved Power Treats, backend, authentication,
multiplayer, battle pass,
online leaderboard, real-money store, diagonal matching, full-row clearing, rigid-body physics,
or ad SDK. The approved achievements, daily goals, leaderboard, earned Sugar Stars, cosmetic shop,
Power Treats, and Gummy Blitz are local-only.
