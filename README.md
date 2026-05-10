# Shipwrecked 🌊

A text-based survival RPG for Android, built entirely in Java using Android Studio Panda 4.

## About the Game

You are the sole survivor of a shipwreck on a deserted island. Each day you must
gather resources, manage your health and hunger, craft survival tools, and explore
the island. All major actions are resolved with animated dice rolls.

Build a raft or light a signal fire to escape. Survive 30 days — or perish trying.

## Gameplay Features

- **5 endings** — Escape, Rescued, Island Master (hidden), Stranded, and Perished
- **8 daily actions** — Forage, Hunt, Fish, Gather Wood, Gather Vine, Explore, Rest, Build Raft
- **Animated D6 dice rolls** with procedurally generated sound effects
- **Crafting system** — 5 craftable items that improve survival odds
- **Random exploration events** — 8 possible events including storms, discoveries, and wildlife
- **Survival stats** — Health, Hunger, and Energy tracked with live progress bars
- **Ambient ocean sound** generated procedurally in code (no audio files required)
- **Day/night progression** with hunger drain and weather consequences

## Technical Details

| Detail | Value |
|---|---|
| Language | Java |
| IDE | Android Studio Panda 4 (2025.3.4) |
| Minimum SDK | API 26 (Android 8.0) |
| Architecture | MVVM (Model-View-ViewModel) |
| Audio | AudioTrack with procedural PCM generation |
| Animations | ValueAnimator, AnimationUtils |
| UI | Material Design 3 components |
| Persistence | Application-scoped ViewModel |

## Architecture Overview

```
com.bdsoftware.shipwrecked/
├── model/
│   ├── GameState.java          Core data container for all player stats
│   ├── ActionResult.java       Result object returned after every dice roll
│   └── CraftingRecipe.java     Data class representing a craftable item
├── viewmodel/
│   └── GameViewModel.java      Game logic, dice rolling, crafting, endings
├── util/
│   └── SoundManager.java       Procedural audio generation and playback
├── MainActivity.java           Title screen
├── GameActivity.java           Main game loop screen
├── CraftingActivity.java       Crafting menu screen
├── EndingActivity.java         Game over / ending screen
├── CraftingAdapter.java        RecyclerView adapter for recipe list
└── ShipwreckedApp.java         Application class for shared ViewModel scope
```

## Crafting Recipes

| Item | Wood | Vine | Flint | Effect |
|---|---|---|---|---|
| Fishing Rod | 2 | 1 | 0 | +1 to Fish dice rolls |
| Spear | 3 | 1 | 0 | +1 to Hunt dice rolls |
| Shelter | 5 | 3 | 0 | Reduces storm damage |
| Signal Fire | 4 | 0 | 1 | Rescue arrives in 3 days |
| Water Collector | 2 | 0 | 0 | Slows hunger drain |

## Dice Roll Outcomes

| Roll | Outcome | Effect |
|---|---|---|
| 1 | Critical Fail | Injury or resource loss |
| 2-3 | Fail | No resources gained |
| 4-5 | Success | Standard resources gained |
| 6 | Critical Success | Bonus resources or discovery |

## How to Build

1. Clone this repository
2. Open in Android Studio (Panda 4 or later recommended)
3. Let Gradle sync complete
4. Run on an emulator or physical device running Android 8.0 or above

```bash
git clone https://github.com/yourusername/shipwrecked-android.git
```

## Screenshots

*Coming soon*

## What I Learned

Building Shipwrecked taught me:

- MVVM architecture and how to share a ViewModel across multiple Activities
- Procedural audio generation using Android's AudioTrack API
- Android Vector Drawable format and its differences from standard SVG
- RecyclerView with a custom Adapter and ViewHolder pattern
- LiveData observation and reactive UI updates
- Git version control and Android project structure for portfolio presentation

## Licence

MIT Licence — free to use, modify, and distribute with attribution.