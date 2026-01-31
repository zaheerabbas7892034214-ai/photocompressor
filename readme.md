# Photo Compressor – KB Size

A complete Android application for compressing images to a target file size in KB.

## Features

- **Image Compression**: Compress images to target KB size with best-effort approach
- **Smart Algorithm**: Quality reduction (92-30) followed by dimension scaling (0.9 factor) if needed
- **Image Picker**: Select images using ActivityResultContracts.GetContent
- **Gallery Save**: Save compressed images to device gallery using MediaStore
- **Image Sharing**: Share compressed images with FileProvider implementation
- **Pro Version**: In-app purchase support with Play Billing Library v7.x
- **No Storage Permission Required**: Uses scoped storage for Android 10+

## Tech Stack

- **Language**: Kotlin
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **Architecture**: MVVM (ViewModel + StateFlow)
- **UI**: Jetpack Compose + Material 3
- **Navigation**: Navigation Compose
- **Single Activity**: Modern Android architecture
- **Build System**: Groovy Gradle (NOT Kotlin DSL)
- **Billing**: Play Billing Library v7.0.0 (billing-ktx)
- **Image Loading**: Coil for Compose

## Project Structure

```
app/
├── src/main/
│   ├── java/com/zaheer/photocompressor/
│   │   ├── domain/
│   │   │   └── ExactKbCompressor.kt      # Core compression engine
│   │   ├── data/
│   │   │   └── PreferencesRepository.kt  # SharedPreferences for Pro status
│   │   ├── billing/
│   │   │   └── BillingManager.kt         # Play Billing integration
│   │   ├── presentation/
│   │   │   ├── home/
│   │   │   │   ├── HomeScreen.kt         # Main UI screen
│   │   │   │   └── HomeViewModel.kt      # Home screen ViewModel
│   │   │   └── pro/
│   │   │       ├── ProScreen.kt          # Pro version screen
│   │   │       └── ProViewModel.kt       # Pro screen ViewModel
│   │   ├── ui/theme/                     # Material 3 theme
│   │   └── MainActivity.kt               # Single activity with Navigation
│   └── res/
│       ├── values/
│       │   ├── strings.xml
│       │   └── themes.xml
│       └── xml/
│           └── file_paths.xml            # FileProvider configuration
```

## Core Compression Algorithm

The `ExactKbCompressor` implements a sophisticated two-phase compression approach:

### Phase 1: Quality Reduction
- Start with quality 92
- Reduce by 5 until quality 30
- Return if target size achieved

### Phase 2: Dimension Scaling
- Scale dimensions by 0.9 factor iteratively
- Minimum scale: 0.3 (to maintain quality)
- Re-apply quality loop at each scale level

### Features
- Safe bitmap decoding with downsampling
- Memory-efficient processing
- Approximate flag when exact target not achievable
- Returns: ByteArray, size KB, quality, scale, and approximate flag

## Play Billing Integration

- **Product ID**: `photo_compressor_pro`
- **Type**: In-app purchase (INAPP)
- **Features**:
  - BillingClient initialization on app launch
  - ProductDetails query and display
  - Purchase flow handling
  - Purchase acknowledgment
  - Restore purchases functionality
  - SharedPreferences persistence (`is_pro`)
  - Error handling and resilience

## Building the Project

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Install on connected device
./gradlew installDebug
```

## Dependencies

- AndroidX Core, Lifecycle, Activity
- Jetpack Compose (BOM 2023.10.01)
- Material 3
- Navigation Compose 2.7.5
- ViewModel Compose 2.6.2
- Play Billing 7.0.0
- Kotlin Coroutines 1.7.3
- Coil Compose 2.5.0

## License

This project is created for educational purposes.
