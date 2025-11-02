# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is an Android home screen widget that displays Google Calendar events with a transparent background and compact text. It reads calendar events from the device and displays them in a scrollable list, highlighting today's events in green and alternating gray/transparent backgrounds for different dates.

## Build Commands

### Build APK
```bash
./gradlew assembleDebug    # Debug APK
./gradlew assembleRelease  # Release APK
```

### Install to Device
```bash
# Install debug APK (requires adb and connected device)
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Check Gradle Tasks
```bash
./gradlew tasks
```

## Release Process

**The release process is fully automated via GitHub Actions** (.github/workflows/release.yml):

1. Update version in `app/build.gradle`:
   ```gradle
   defaultConfig {
       versionCode 10
       versionName "1.0.11"
   }
   ```

2. Commit and push to main:
   ```bash
   git add app/build.gradle
   git commit -m "Bump version to 1.0.11"
   git push origin main
   ```

3. GitHub Actions automatically:
   - Extracts version from build.gradle
   - Creates git tag (e.g., `v1.0.11`) if it doesn't exist
   - Builds release APK
   - Creates GitHub release with APK attached as `calendar-widget-v1.0.11.apk`

**Important**: The workflow only triggers when `app/build.gradle` is modified and pushed to main.

## Architecture

### Core Components

1. **MainActivity** (`MainActivity.kt`)
   - Entry point that requests READ_CALENDAR permission
   - Displays current version and provides link to GitHub releases
   - Triggers widget updates after permission is granted

2. **CalendarWidgetProvider** (`CalendarWidgetProvider.kt`)
   - AppWidgetProvider that manages widget lifecycle
   - Handles widget updates (automatic every 30 minutes + manual refresh)
   - Sets up click handlers:
     - Title click: Opens calendar app
     - Item click: Opens specific event in calendar
     - Refresh button: Manually refreshes widget data
   - Listens for date/time/timezone changes to auto-update

3. **CalendarWidgetService** (`CalendarWidgetService.kt`)
   - RemoteViewsService that provides data to the widget's ListView
   - Queries CalendarContract.Events for events from today to 3 months ahead
   - Implements date-alternating background colors (gray/transparent)
   - Highlights today's events with green background (#66228B22)
   - Limits display to 20 events maximum
   - Shows error messages when permissions are missing or no events exist

### Data Flow

```
User adds widget → CalendarWidgetProvider.onUpdate()
                → Sets RemoteAdapter to CalendarWidgetService
                → CalendarWidgetService.onDataSetChanged()
                → Queries CalendarContract.Events
                → Returns RemoteViews for each event
                → Widget displays ListView
```

### Permission Handling

The app requires `android.permission.READ_CALENDAR`:
- Requested by MainActivity on first launch
- Service checks permission before querying calendar
- Shows helpful error messages in widget if permission denied

### Key Design Patterns

- **RemoteViews Pattern**: Widget UI is built using RemoteViews for cross-process rendering
- **RemoteViewsService**: Used for efficient ListView population in widgets
- **PendingIntent Templates**: Each event item uses fillInIntent to open specific calendar events
- **Date Change Broadcasting**: Widget auto-updates on system date/time changes via BroadcastReceiver

## Technical Requirements

- **Minimum SDK**: 26 (Android 8.0)
- **Target SDK**: 34 (Android 14)
- **Java Version**: 17
- **Language**: Kotlin
- **Key Dependencies**:
  - androidx.core:core-ktx:1.12.0
  - androidx.appcompat:appcompat:1.6.1
  - com.google.android.material:material:1.10.0

## UI Characteristics

- **Background**: Fully transparent (#00000000)
- **Text sizes**: Title 12sp, event details 10sp
- **Date format**: MM/dd(E) in Korean
- **Time format**: HH:mm (24-hour), empty for all-day events
- **Today highlight**: Dark green background (#66228B22)
- **Date alternation**: First date has gray background (#33FFFFFF), second date transparent, repeating

## Testing the Widget

1. Build and install app
2. Grant calendar permission in MainActivity
3. Long-press home screen → Add widget → "Calendar Widget"
4. Widget should display events from Google Calendar
5. Tap events to open in calendar app
6. Tap title to open calendar
7. Tap refresh button to manually update

## Debugging

All components use extensive logging with tag "CalendarWidget". Check logcat:
```bash
adb logcat -s CalendarWidget:*
```

Key log locations:
- MainActivity: Permission requests and widget update triggers
- CalendarWidgetProvider: Widget lifecycle and update calls
- CalendarWidgetService: Calendar event queries and data loading (uses Log.e for visibility)
