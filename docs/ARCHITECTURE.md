# Architecture

## Boundaries

The project is a single Android application module, but code follows strict source-level
boundaries:

- `game/model`: immutable domain models and centralized configuration.
- `game/engine`: deterministic pure Kotlin systems and the orchestration engine.
- `presentation`: Android ViewModel, UI state flow, frame-delta bridge, persistence coordination.
- `data`: pure meta-progress rules/models plus DataStore-backed settings and statistics.
- `feedback`: Android audio and haptic implementation driven by presentation events.
- `ui`: Compose screens, candy UI kit, Canvas board renderer, gestures, and visual effects.

Only presentation/data/feedback/UI know Android. The `game` packages must remain JVM-testable.

## State ownership

`GameEngine` owns a `GameState` value and returns a new value plus `GameEvent`s for every input
or time advancement. It delegates shape/candy generation, collision, rotation, match detection,
gravity, scoring, and bomb geometry to cohesive systems. Randomness is injected through a small
pure Kotlin `RandomSource` boundary so tests can be deterministic.

The ViewModel serializes engine actions on the main thread, exposes `StateFlow`, maps domain
events to feedback, and persists only meta progress. Compose never mutates board cells directly.

`MetaProgressRules` is deterministic and Android-free. It owns daily goal selection, local-day
rollover, streak updates, achievement derivation, aggregate statistics, and top-10 leaderboard
ordering. `ProgressStore` only encodes/decodes those values. The leaderboard is intentionally
device-local; there is no backend, identity, or network boundary.

`ShopRules` is also deterministic and Android-free. It validates catalog IDs, prevents duplicate
or unaffordable purchases, normalizes default ownership, restricts equip actions to owned items,
and calculates capped run rewards. The renderer consumes equipped cosmetic IDs; the engine never
depends on them.

Power Treat inventory remains in the offline progress layer, while the three treat effects live in
`GameEngine.usePowerTreat`. A treat is consumed only after the engine emits `PowerTreatUsed`.
`GummyBlitzEngine` is a separate pure Kotlin state machine with explicit READY, PLAYING, PAUSED,
and FINISHED phases; Compose supplies frame deltas but never determines scoring correctness.

## Timing

Compose supplies monotonic frame deltas. The engine accumulates auto-fall and lock-delay time.
Resolution is computed synchronously and deterministically; the UI may animate the returned
events without delaying the engine. Backgrounding forces the explicit paused state.

## Rendering

The board renderer computes a cell size from actual available width/height, clips to the 14
visible rows, preloads gummy painters, and draws static cells, ghost, active cells, grid treatment,
and danger border. Hold/Next use the same shape layout data and exact candy composition. The
Ghost Piece and Reduce Motion settings directly control the renderer and transitions. Equipped
board themes change only renderer colors; equipped drop effects add deterministic glints, glows,
or ghost styling without affecting game state or timing.

## Reference audit

ThunderStack and Volcanix established a reliable Compose/Java 17/Gradle stack, lifecycle pause,
DataStore, feedback-controller, state-flow, and Canvas patterns. This project keeps those sound
patterns while splitting the domain into smaller systems, keeping the approved offline meta layer
narrow, keeping original design art out of runtime resources, and avoiding a giant UI atlas or
god screen.
