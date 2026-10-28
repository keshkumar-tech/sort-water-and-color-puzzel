# 🧪 Water Sort & Color Puzzle Pro

A polished, fully native **Android water-sorting puzzle game** built with Java and the Android SDK. Pour colored liquids between bottles until every bottle holds a single, uniform color — with premium animations, sound effects, haptic feedback, and an unlimited level system.

<p align="center">
  <img alt="Platform" src="https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white">
  <img alt="Language" src="https://img.shields.io/badge/Language-Java-ED8B00?logo=openjdk&logoColor=white">
  <img alt="Min SDK" src="https://img.shields.io/badge/Min%20SDK-24-blue">
  <img alt="Target SDK" src="https://img.shields.io/badge/Target%20SDK-36-blue">
  <img alt="License" src="https://img.shields.io/badge/License-MIT-green">
</p>

---

## 📖 About the Game

**Water Sort & Color Puzzle Pro** is a color-sorting logic game. Each bottle can hold **4 layers** of liquid. Your goal is simple:

> Sort the colored water so that **every bottle contains only one color** (or is completely empty).

You can only pour the **top color** of a bottle into another bottle if:
- The destination bottle is **empty**, or
- The destination bottle's **top color matches** the color being poured, and
- The destination bottle **has free space**.

The moment a bottle is fully sorted, it is **capped** with a satisfying animation and locked.

---

## ✨ Features

| Feature | Description |
| :--- | :--- |
| 🎯 **Unlimited Levels** | Procedurally generated levels — the difficulty scales forever. No hard-coded level cap. |
| 🧩 **Dynamic Board Layouts** | Standard 2-row layout for early levels, staggered 3-column layout from **Level 10+**. |
| ⏪ **Undo Move** | Full history stack — step back through every pour you made. |
| 🔄 **Restart Level** | Instantly regenerate the current level with a fresh shuffle. |
| ➕ **Add Extra Bottle** | Stuck? Boost yourself with one additional empty bottle per level. |
| 🔊 **Sound Effects** | Crisp tone-based feedback for pick, pour, win, and UI actions. |
| 📳 **Haptic Feedback** | Vibration cues on selection, pour, and bottle completion. |
| ⚙️ **Settings Panel** | Toggle sound and vibration on/off at any time. |
| 🏆 **Victory Overlay** | Animated celebration screen with a "Next Level" button. |
| 🎨 **Premium Visuals** | Glassmorphism UI, gradient backgrounds, liquid layers with rounded bottoms, sparkle + bounce animations. |
| 📱 **Edge-to-Edge Display** | Full immersive experience with proper system bar insets handling. |
| ♿ **RTL Support** | Right-to-left layout support enabled. |

---

## 🎮 How to Play

1. **Launch** the app and tap **Start** on the splash screen.
2. **Tap a bottle** to pick up its top color layer.
3. **Tap another bottle** to pour into it.
4. **Repeat** until every filled bottle is a single solid color.
5. Complete the level → tap **Next Level** to continue with increased difficulty.
6. Use the toolbar buttons for **Restart**, **Undo**, **Add Bottle**, and **Settings**.

### Rules at a Glance

```
✅ Allowed                        ❌ Not Allowed
─────────────────────────         ──────────────────────────
Pour into an empty bottle         Pour into a full bottle (4/4)
Pour onto the same color          Pour onto a different color
Pour into a bottle with space     Pour from a capped/solved bottle
```

---

## 🧠 Level Design & Difficulty

| Level | Colors | Empty Bottles | Layout |
| :---: | :---: | :---: | :--- |
| 1 | 2 | 1 | Standard (2 rows) |
| 2 | 3 | 2 | Standard (2 rows) |
| 3 | 4 | 2 | Standard (2 rows) |
| 4–9 | scales with level | scales with level | Standard (2 rows) |
| 10+ | up to 6 (max palette) | scales with level | Advanced (3-column staggered) |

The color pool is shuffled with `Collections.shuffle()` on every level load, so **no two playthroughs are identical**.

---

## 🏗️ Project Structure

```
SortWaterAndColorPuzzelPro/
├── app/
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/example/sortwaterandcolorpuzzelpro/
│       │   ├── StartActivity.java        # Splash / launcher screen
│       │   └── MainActivity.java         # Core game engine & UI logic
│       └── res/
│           ├── anim/                     # scale_up entry animation
│           ├── drawable/                 # Bottles, caps, gradients, glow, sparkles
│           ├── layout/
│           │   ├── activity_start.xml    # Splash layout
│           │   ├── activity_main.xml     # Game board layout
│           │   ├── bottle_item.xml       # Single bottle component
│           │   ├── layout_settings.xml   # Settings overlay
│           │   └── layout_victory.xml    # Victory overlay
│           ├── mipmap-*/                 # Launcher icons
│           └── values/                   # Colors, strings, themes
├── gradle/libs.versions.toml             # Version catalog
├── build.gradle.kts                      # Root build script
├── settings.gradle.kts                   # Module configuration
└── README.md
```

### Class Overview

| Class | Responsibility |
| :--- | :--- |
| `StartActivity` | Splash screen; navigates to the game on **Start**. |
| `MainActivity` | Game state, level generation, pour validation, animations, sound/haptics, undo history. |
| `MainActivity.Bottle` | Inner model class: holds color layers, view reference, and `isSorted()` logic. |

---

## 🛠️ Tech Stack

- **Language:** Java 11
- **Framework:** Android SDK (AppCompat + Material Components)
- **Build System:** Gradle (Kotlin DSL) with Version Catalog
- **Android Gradle Plugin:** 9.0.1
- **Key Libraries:**
  - `androidx.appcompat:appcompat` 1.7.1
  - `com.google.android.material:material` 1.13.0
  - `androidx.activity:activity` 1.13.0
  - `androidx.constraintlayout:constraintlayout` 2.2.1

No third-party game engine — everything is drawn with **native Android views and View Animators**, keeping the APK small and the startup instant.

---

## 🚀 Getting Started

### Prerequisites

- **Android Studio** (latest stable recommended)
- **JDK 11** or newer
- **Android SDK** with API Level 36 installed
- An emulator or physical device running **Android 7.0 (API 24)** or higher

### Installation

```bash
# 1. Clone the repository
git clone https://github.com/keshkumar-tech/sort-water-and-color-puzzel.git

# 2. Open the project
cd sort-water-and-color-puzzel
```

Then either:

- **Android Studio:** `File → Open` → select the project folder → wait for Gradle sync → press **Run ▶**
- **Command line:**

```bash
# Windows
gradlew.bat assembleDebug

# macOS / Linux
./gradlew assembleDebug
```

The debug APK will be generated at:

```
app/build/outputs/apk/debug/app-debug.apk
```

Install it on a connected device with:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

> ⚠️ **Note:** `local.properties` is intentionally excluded from version control. Android Studio generates it automatically with your SDK path on first open.

---

## 🎨 Customization

### Change the color palette

Edit `app/src/main/res/values/colors.xml`:

```xml
<color name="liquid_red">#FF4B2B</color>
<color name="liquid_blue">#00D2FF</color>
<color name="liquid_yellow">#FFD200</color>
<color name="liquid_green">#1D976C</color>
<color name="liquid_purple">#8E2DE2</color>
<color name="liquid_orange">#F09819</color>
```

### Change bottle capacity

In `MainActivity.java`:

```java
private final int MAX_CAPACITY = 4;   // change to 5 for harder puzzles
```

> Note: if you change capacity, also update the `isSorted()` check inside the `Bottle` class so it compares against the new capacity instead of the literal `4`.

### Tweak difficulty scaling

Adjust the level logic inside `loadLevel()`:

```java
} else {
    numColors = 4 + (level / 2);
    numEmpty  = 2 + (level / 5);
}
```

---

## 📜 License

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for details.

---

## 👨‍💻 Author

**Kesh Kumar**

- GitHub: [@keshkumar-tech](https://github.com/keshkumar-tech)

---

<p align="center">
  Made with ❤️ and ☕ in Java<br>
  <i>If you enjoy this project, consider giving it a ⭐ on GitHub!</i>
</p>
