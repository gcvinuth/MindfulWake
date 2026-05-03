# 🎯 MindfulWake Kotlin APK - Complete Package

## 📦 What You Got

**One Complete Android App** (Kotlin) with:
- ✅ 5-tab navigation (Alarms, Create, Settings, Stats, Weather)
- ✅ 100+ dynamic quiz questions (Easy/Medium/Hard)
- ✅ Exact alarm scheduling with repeat days
- ✅ Foreground service + notification system
- ✅ Dark theme + Material Design 3
- ✅ Persistent storage (SharedPreferences)
- ✅ Weather integration (Open-Meteo API)
- ✅ GitHub Actions CI/CD workflow

---

## 📥 Download & Extract

**File:** `MindfulWake.zip` (51 KB)

```bash
unzip MindfulWake.zip
cd MindfulWake
```

---

## ⚡ Quick Start (3 Steps)

### Step 1: Open in Android Studio
```
File → Open → Select "MindfulWake" folder
Wait for Gradle sync (~2 min)
```

### Step 2: Build
```bash
./gradlew assembleDebug
```
✅ Output: `app/build/outputs/apk/debug/app-debug.apk`

### Step 3: Install on Phone/Emulator
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## 📂 File Organization

```
MindfulWake/
├── .github/workflows/build-apk.yml        ← GitHub Actions (auto-build on push)
├── app/src/main/
│   ├── java/com/mindfulwake/app/
│   │   ├── MindfulWakeApp.kt
│   │   ├── alarm/                         ← Alarm system (4 files)
│   │   ├── data/                          ← Models + Questions (3 files)
│   │   └── ui/                            ← All screens (7 fragments + 1 activity)
│   └── res/
│       ├── layout/                        ← 11 XML layouts
│       ├── values/                        ← Colors, strings, themes
│       ├── drawable/                      ← Icons & drawables
│       └── menu/                          ← Bottom nav menu
├── build.gradle.kts                       ← Root gradle
├── settings.gradle.kts
└── gradlew                                ← Gradle wrapper (auto-generated)
```

**Total: 40+ Kotlin/XML files, ~4000 lines of code**

---

## 🔑 Key Components

### **Alarm System**
- `AlarmScheduler.kt` - Schedules exact alarms (Android 12+ compatible)
- `AlarmService.kt` - Plays tone + vibration in foreground
- `AlarmReceiver.kt` - Triggered by AlarmManager
- `BootReceiver.kt` - Auto-reschedule on device reboot

### **Quiz Engine**
- `QuestionBank.kt` - 1200+ lines: 100+ questions across 6 categories
- `AlarmRingActivity.kt` - 3-phase flow: Ringing → Quiz → Results

### **Persistence**
- `Prefs.kt` - Wrapper around SharedPreferences for alarms/settings/stats
- `Models.kt` - Data classes (Alarm, Settings, Stats, etc)

### **UI (5 Tabs)**
1. **Alarms** - List, toggle, delete
2. **Create** - Time picker, difficulty, repeat days, label
3. **Settings** - Sound, snooze, penalty, timer, bedtime
4. **Stats** - Accuracy, streak, fastest time, history
5. **Weather** - Open-Meteo API, geolocation, city search

---

## 🚀 Build Options

### **Local Debug Build**
```bash
./gradlew assembleDebug
```
→ `app/build/outputs/apk/debug/app-debug.apk`

### **GitHub Actions (Recommended)**
1. Push to `main`/`master` branch
2. GitHub automatically builds APK
3. Download from Actions → Artifacts

### **Release Build** (with signing)
See comments in `.github/workflows/build-apk.yml` for keystore setup

---

## 📋 File Checklist

| Category | Count | Files |
|----------|-------|-------|
| **Kotlin Classes** | 11 | App, Alarms (4), UI (7) |
| **Data/Models** | 3 | Models, Prefs, QuestionBank |
| **XML Layouts** | 11 | Main, fragments (5), alarm phases (3), item |
| **Resources** | 4 | Colors, strings, themes, menu |
| **Drawables** | 2 | Card BG, alarm icon |
| **App Icons** | 10 | Launcher across 5 densities |
| **Config** | 3 | build.gradle, settings.gradle, gradle.properties |
| **CI/CD** | 1 | GitHub Actions workflow |
| **Manifest** | 1 | AndroidManifest.xml |

---

## ✅ Minimum Requirements

- **Android Studio** latest
- **JDK 17+**
- **Android SDK** API 26-34
- **Gradle 8.0+** (bundled)
- **Min Android Version:** 8.0 (API 26)
- **Target Android Version:** 14 (API 34)

---

## 🔐 Permissions

```xml
RECEIVE_BOOT_COMPLETED       ← Reschedule alarms after reboot
SCHEDULE_EXACT_ALARM         ← Exact alarm scheduling
USE_EXACT_ALARM              ← Android 12+ requirement
VIBRATE                      ← Vibration on alarm
WAKE_LOCK                    ← Keep screen on
FOREGROUND_SERVICE           ← Foreground alarm service
POST_NOTIFICATIONS           ← Notifications
INTERNET                     ← Weather API
ACCESS_FINE_LOCATION         ← Geolocation
```

---

## 📱 Install & Run

```bash
# 1. Build
./gradlew assembleDebug

# 2. Install via ADB
adb install -r app/build/outputs/apk/debug/app-debug.apk

# 3. Launch on device
adb shell am start -n com.mindfulwake.app/.ui.MainActivity

# Or: Drag APK into emulator window
```

---

## 🎨 Theme Colors

| Component | Color | Hex |
|-----------|-------|-----|
| Background | Dark | #0A0A14 |
| Card | Slightly lighter | #12121E |
| Primary text | Light | #F0EFF5 |
| Secondary text | Muted | #8B8A9B |
| Accent (Primary) | Purple | #A78BFA |
| Accent (Teal) | Teal | #2DD4BF |
| Accent (Amber) | Amber | #FBB03B |

---

## 🔧 Troubleshooting

**"Gradle sync failed"**
```bash
./gradlew clean
# Delete .gradle folder
# Resync in Android Studio
```

**"API level not found"**
→ SDK Manager → Install API 34

**"gradlew permission denied"**
```bash
chmod +x gradlew
./gradlew assembleDebug
```

**APK won't install**
→ Check min API 26 (Android 8.0)

---

## 📊 Code Statistics

- **Total Files:** 40+
- **Kotlin Lines:** ~2000
- **XML Lines:** ~1500
- **JSON Config:** ~200
- **Total:** ~3700+ lines

---

## 🚢 Deploy to Play Store (Future)

1. Create signed release APK (add keystore secrets to GitHub)
2. Uncomment release build section in `.github/workflows/build-apk.yml`
3. Create Google Play Developer account
4. Upload APK + Store Listing

---

## 📞 Support & Next Steps

1. ✅ Extract ZIP
2. ✅ Open in Android Studio
3. ✅ Sync Gradle
4. ✅ Build APK (`./gradlew assembleDebug`)
5. ✅ Install on device (`adb install ...apk`)
6. ✅ Test all features
7. ✅ Deploy via GitHub Actions (optional)

---

**Ready to build? Start here:** `DOWNLOAD_GUIDE.md`
