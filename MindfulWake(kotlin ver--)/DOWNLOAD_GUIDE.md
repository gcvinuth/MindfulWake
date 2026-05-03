# MindfulWake Kotlin Android App - File Download Guide

## 📦 Complete File Structure

```
MindfulWake/                                    # Root Project
├── .github/
│   └── workflows/
│       └── build-apk.yml                       # GitHub Actions CI/CD
│
├── app/
│   ├── build.gradle.kts                        # App gradle config
│   ├── proguard-rules.pro
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/com/mindfulwake/app/
│   │   │   ├── MindfulWakeApp.kt               # Application class
│   │   │   ├── alarm/
│   │   │   │   ├── AlarmScheduler.kt           # Exact alarm scheduling
│   │   │   │   ├── AlarmReceiver.kt            # Broadcast receiver
│   │   │   │   ├── AlarmService.kt             # Foreground service (ringing)
│   │   │   │   └── BootReceiver.kt             # Boot completed receiver
│   │   │   ├── data/
│   │   │   │   ├── Models.kt                   # Data classes (Alarm, Stats, etc)
│   │   │   │   ├── Prefs.kt                    # SharedPreferences wrapper
│   │   │   │   └── QuestionBank.kt             # All 100+ quiz questions (port from JS)
│   │   │   └── ui/
│   │   │       ├── MainActivity.kt             # Tab host activity
│   │   │       ├── AlarmsFragment.kt           # List & manage alarms
│   │   │       ├── CreateAlarmFragment.kt      # Create new alarm
│   │   │       ├── SettingsFragment.kt         # App settings
│   │   │       ├── StatsFragment.kt            # Performance stats
│   │   │       ├── WeatherFragment.kt          # Open-Meteo weather
│   │   │       └── AlarmRingActivity.kt        # Ring → Quiz → Complete flow
│   │   └── res/
│   │       ├── layout/                         # XML layouts (11 files)
│   │       │   ├── activity_main.xml
│   │       │   ├── fragment_alarms.xml
│   │       │   ├── fragment_create_alarm.xml
│   │       │   ├── fragment_settings.xml
│   │       │   ├── fragment_stats.xml
│   │       │   ├── fragment_weather.xml
│   │       │   ├── activity_alarm_ring.xml
│   │       │   ├── phase_ringing.xml
│   │       │   ├── phase_quiz.xml
│   │       │   ├── phase_complete.xml
│   │       │   └── item_alarm.xml
│   │       ├── values/                         # Resources
│   │       │   ├── colors.xml
│   │       │   ├── strings.xml
│   │       │   └── themes.xml
│   │       ├── drawable/                       # Drawable resources
│   │       │   ├── card_bg.xml
│   │       │   └── ic_alarm.xml
│   │       ├── menu/
│   │       │   └── bottom_nav_menu.xml
│   │       └── mipmap-*/                       # App icon densities
│   │           ├── ic_launcher.xml
│   │           └── ic_launcher_round.xml
│   └── build/                                  # (Generated after build)
│       └── outputs/apk/debug/
│           └── app-debug.apk                   # Final APK
│
├── build.gradle.kts                            # Root gradle config
├── settings.gradle.kts                         # Gradle settings
├── gradle.properties                           # Gradle properties
└── gradlew / gradlew.bat                       # Gradle wrapper (auto-generated)
```

## 🚀 Quick Setup Instructions

### 1️⃣ **Clone or Download**
   - Clone repo: `git clone <repo-url>`
   - Or download as ZIP and extract

### 2️⃣ **Open in Android Studio**
   ```bash
   # Ensure you have:
   # - Android Studio (latest)
   # - JDK 17+ installed
   # - Android SDK (min API 26, target API 34)
   ```
   - File → Open → Select `MindfulWake` folder
   - Wait for Gradle sync (~2 min)

### 3️⃣ **Build Debug APK Locally**
   ```bash
   ./gradlew assembleDebug
   # Output: app/build/outputs/apk/debug/app-debug.apk
   ```

### 4️⃣ **Build via GitHub Actions (CI/CD)**
   - Push to `main` or `master` branch
   - GitHub Actions automatically builds APK
   - Download from Actions → Artifacts (within 30 days)

---

## 📋 File-by-File Breakdown

### **Core Data Layer** (`data/`)
| File | Lines | Purpose |
|------|-------|---------|
| `Models.kt` | 50 | Alarm, Settings, Stats data classes |
| `Prefs.kt` | 50 | SharedPreferences CRUD wrapper |
| `QuestionBank.kt` | 1200+ | All 100+ quiz questions (Easy/Medium/Hard) |

### **Alarm System** (`alarm/`)
| File | Purpose |
|------|---------|
| `AlarmScheduler.kt` | exactAlarm() scheduling with Android 12+ checks |
| `AlarmReceiver.kt` | Broadcast receiver from AlarmManager |
| `AlarmService.kt` | Foreground service: plays tone + vibration |
| `BootReceiver.kt` | Re-schedule alarms after device reboot |

### **UI Fragments** (`ui/`)
| Fragment | Purpose | Features |
|----------|---------|----------|
| `AlarmsFragment.kt` | List & manage | Toggle enable/disable, delete, quick view |
| `CreateAlarmFragment.kt` | Create new | Time picker, difficulty, repeat days, label |
| `SettingsFragment.kt` | Settings | Sound, snooze, penalty, timer, bedtime |
| `StatsFragment.kt` | Statistics | Streak, accuracy %, fastest time, history |
| `WeatherFragment.kt` | Weather | Open-Meteo API, geolocation, city search |
| `AlarmRingActivity.kt` | Ring + Quiz | 3 phases: Ringing → Quiz → Complete |

### **Layouts** (`res/layout/`)
- **11 XML files** totaling ~600 lines
- Dark theme (#0A0A14), Purple accent (#A78BFA)
- Material Design 3 components

---

## 🔧 Build & Deploy

### **Local Build**
```bash
# Debug APK
./gradlew assembleDebug

# Release APK (requires keystore setup)
./gradlew assembleRelease
```

**Output:**
```
✅ app/build/outputs/apk/debug/app-debug.apk
```

### **GitHub Actions (Recommended)**
1. Push code to `main` branch
2. Go to Actions tab → Select "Build MindfulWake APK" workflow
3. Workflow auto-runs: JDK setup → Gradle sync → Build APK
4. Download artifact from workflow run (30-day retention)

**For Release APK:**
- Add keystore secrets to GitHub (see comments in `.github/workflows/build-apk.yml`)

---

## 📥 How to Download Files

### **Option A: Single ZIP Download**
```bash
# From project root:
zip -r MindfulWake-complete.zip MindfulWake/
# Download the ZIP from file manager
```

### **Option B: Individual File Copy**
- Open each file in editor
- Copy → Paste into your project

### **Option C: Use Git**
```bash
git clone <your-repo-url>
cd MindfulWake
./gradlew assembleDebug
# APK ready at: app/build/outputs/apk/debug/app-debug.apk
```

---

## ✅ Pre-Build Checklist

- [ ] Java 17+ installed
- [ ] Android SDK (API 26-34)
- [ ] Android Studio latest version
- [ ] `gradlew` has execute permissions: `chmod +x gradlew`
- [ ] Internet connection (for Gradle dependency download)

---

## 📱 Install APK on Device

```bash
# Via ADB
adb install app/build/outputs/apk/debug/app-debug.apk

# Or: Drag APK into Android Studio device emulator
# Or: Copy to phone & open with file manager
```

---

## 🎯 Key Features Implemented

✅ **Alarm Management**
- Create, enable/disable, delete alarms
- Exact alarm scheduling (Android 12+)
- Repeat on specific days
- Bedtime reminders

✅ **Quiz Engine**
- 100+ dynamic questions (Easy/Medium/Hard)
- 6 categories: Math, Logic, Awareness, Pattern, Language, Custom
- Smart difficulty auto-adjust based on accuracy
- Penalty system (wrong answer = +1 question)
- Question timer (optional 15s)

✅ **Audio & Notifications**
- Programmable tone patterns (classic, gentle, nature, urgent)
- Vibration patterns
- Foreground notification with full-screen intent
- Custom tone upload (future feature)

✅ **Statistics & Analytics**
- Accuracy tracking
- Session history (last 5)
- Weekly performance chart
- Fastest time tracking
- Streak counter

✅ **Additional Features**
- Weather integration (Open-Meteo API)
- Settings: snooze, penalty, timer, bedtime offset
- Dark theme (#0A0A14)
- Material Design 3

---

## 🛠️ Troubleshooting

| Issue | Solution |
|-------|----------|
| "Gradle sync failed" | Delete `.gradle` folder, resync |
| "gradlew not found" | `chmod +x gradlew` then try again |
| "API 34 not installed" | SDK Manager → SDK Platforms → Install API 34 |
| "Compilation error in QuestionBank" | Ensure Kotlin plugin is up to date |
| APK not installing | Check Android version (min API 26 = Android 8.0) |

---

## 📞 Support

For issues:
1. Check Kotlin/Android Studio version compatibility
2. Clear gradle cache: `./gradlew clean`
3. Rebuild: `./gradlew assembleDebug`

---

Generated: 2026-05-03
MindfulWake v1.0 Kotlin Edition
