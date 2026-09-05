# Asset Manifest

## Visual audit

The supplied visual references establish three related directions:

- A candy-land scene with frosted hills, syrup surfaces, sprinkles, gummy bears, and mint/pink sky.
- A smooth glossy hot-pink panel and a softly blurred sprinkle gradient suitable for gameplay UI.
- Glossy translucent green/purple/red bears, a green star, orange heart, pink round bomb, and
  pill-shaped purple/orange panels with white/purple rims.
- Promo art reinforces saturated pink/purple framing, oversized candy forms, soft specular shine,
  chunky cream lettering, and very dense decoration. Gameplay will keep that material language
  but reduce density so the 8×14 board remains legible.

Board geometry, toggle tracks, glows, particles, ghost treatment, and danger pulse remain
CODE-DRAWN. Reusable candy rims are small dedicated assets rendered with three-/nine-slice
scaling so glossy caps, cream icing, and corner candies never stretch.

The final menu mascot showcase, live status badges, and illustrated button treatments reuse the
verified individual gummy assets with Compose layout/geometry; no redundant composite raster is shipped.

| Name | Purpose | Source | Class | Status | Format / master | Alpha | Used on | Notes |
|---|---|---|---|---|---|---|---|---|
| `background_reference.png` | Source visual sheet | User-supplied `back ground.png` | ORIGINAL | Reference only | PNG, original size | No | Design only | Immutable copy; contains bg-01, bg-02, bg-03 |
| `gummy_ui_reference.png` | Source gummy/UI sheet | User-supplied `ui mishka.png` | ORIGINAL | Reference only | PNG, original size | No | Design only | Immutable copy; objects 01–16 |
| `promo_reference.png` | Icon/promo visual sheet | User-supplied `black appname.png` | ORIGINAL | Reference only | PNG, original size | No | Design only | Immutable copy; not shipped wholesale |
| `bg_candy_land.webp` | Main/menu background | AI recreation from bg-01 direction | GENERATED | Final | WebP, 1080×1920 | No | Menu | Centre kept quiet for readable controls |
| `bg_sprinkles.webp` | Gameplay background | Processed bg-03 crop | CROP | Final | WebP, 1080×1920 | No | Gameplay, Settings | Soft focus; no board interference |
| `gummy_green_bear.webp` | Normal candy | AI recreation of gummy object 01 | GENERATED | Final | WebP, 256×256 | Yes | Board, Next, Hold |
| `gummy_purple_bear.webp` | Normal candy | Colour-authored variant of green bear | RECREATED | Final | WebP, 256×256 | Yes | Board, Next, Hold |
| `gummy_red_bear.webp` | Normal candy | Colour-authored variant of green bear | RECREATED | Final | WebP, 256×256 | Yes | Board, Next, Hold |
| `gummy_green_star.webp` | Level 4+ candy | AI recreation of gummy object 04 | GENERATED | Final | WebP, 256×256 | Yes | Board, Next, Hold |
| `gummy_orange_heart.webp` | Level 7+ candy | AI recreation of gummy object 05 | GENERATED | Final | WebP, 256×256 | Yes | Board, Next, Hold |
| `gummy_pink_bomb.webp` | Combo reward bomb | AI recreation of gummy object 06 | GENERATED | Final | WebP, 256×256 | Yes | Board, Next, reward feedback |
| `ui_home_button.webp` | Home navigation button | AI-generated from supplied gummy UI direction | GENERATED | Final | WebP, 256×256 | Yes | Pause, Game Over, Victory |
| `ui_back_button.webp` | Back navigation button | AI-generated from supplied gummy UI direction | GENERATED | Final | WebP, 256×256 | Yes | Settings, Daily, Achievements, Leaderboard, Shop |
| `ui_music_icon.webp` | Music settings icon | AI-generated from supplied gummy UI direction | GENERATED | Final | WebP, 256×256 | Yes | Settings |
| `ui_sound_icon.webp` | Sound effects settings icon | AI-generated from supplied gummy UI direction | GENERATED | Final | WebP, 256×256 | Yes | Settings |
| `ui_button_frame.webp` | Reusable glossy button/stat rim | AI recreation of supplied items 07/09/11, alpha-cleaned | GENERATED | Final | Lossless WebP, 1024×319 | Yes | Menu, HUD, shop, meta actions, dialogs | Three-slice renderer preserves rounded jelly caps |
| `ui_panel_frame.webp` | Reusable decorated content/board frame | AI recreation of supplied item 10 direction, alpha-cleaned | GENERATED | Final | Lossless WebP, 1024×737 | Yes | Menu, gameplay board, meta screens, shop, pause/results | Nine-slice renderer preserves four candy corners |
| `ui_round_button.webp` | Circular glossy control base | Clean crop of supplied UI item 12 | CROP | Final | Lossless WebP, 256×256 | Yes | Gameplay pause | Extracted from immutable sheet; pause glyph remains code-drawn |
| `ui_hold_frame.webp` | Tall Hold preview frame | Clean crop of supplied UI item 13 with violet rim accent | CROP / RECREATED | Final | Lossless WebP, 512×540 | Yes | Gameplay Hold | Nine-slice scaling preserves corners; pale center stays readable |
| `ui_next_frame.webp` | Tall Next preview frame | Clean crop of supplied UI item 15 | CROP | Final | Lossless WebP, 512×534 | Yes | Gameplay Next | Distinct pink rim; nine-slice scaling preserves corners |
| `gummy_victory_bear.webp` | Daily-set victory character | AI-generated from supplied green bear direction | GENERATED | Final | WebP, 512×512 | Yes | Victory result |
| `gummy_loss_bear.webp` | Game-over character | AI-generated from supplied purple bear direction | GENERATED | Final | WebP, 512×512 | Yes | Game Over |
| `app_icon_art.webp`, `ic_launcher*` | Adaptive/legacy launcher icon | Processed supplied promo icon | CROP | Final | WebP + vector + density PNGs | Mixed | Launcher, system splash | Safe padding, distinct round icon, Android monochrome layer |

Original files live only in `design/originals/`. Temporary crops/generation live in ignored
`design_work/`. Final sprites and frame alpha were inspected independently and on-device at 16:9,
18:9, 19.5:9, and 20:9 portrait sizes. `tools/process_assets.py` reproduces the processing without
ever writing to a supplied source file.
