# Photo Compressor – KB Size

A complete Android application built with Kotlin and Jetpack Compose that allows users to compress images to a specific target size in KB.

## Features

### Core Functionality
- **Image Selection**: Pick images using `ActivityResultContracts.GetContent`
- **Target Size Input**: User enters desired file size in KB
- **Smart Compression**: Best-effort compression to achieve target size or closest possible
- **Before/After Comparison**: Display original and compressed file sizes
- **Save to Gallery**: Save compressed images using MediaStore (no storage permission needed)
- **Share Images**: Share compressed images using FileProvider
- **Approximate Results**: Indicates when exact target size is not achievable

### Compression Engine
The `ExactKbCompressor` implements a sophisticated compression algorithm:
1. Safe bitmap decode with automatic downsampling for large images
2. Quality loop from 92% down to 30%
3. Automatic dimension downscaling (by 0.9x iteratively) if target not met
4. Minimum scale limit protection
5. Returns detailed metadata: achieved size, quality used, scale factor, and approximate flag

### Billing Integration
- **Google Play Billing Library v7.x** integration
- One-time managed in-app product: `photo_compressor_pro`
- Features:
  - Unlock Pro button
  - Restore Purchases functionality
  - Pro Active badge in UI
  - Purchase acknowledgment
  - Persistent pro state via SharedPreferences
  - Comprehensive error handling (billing unavailable, item not found, user cancelled)

## Tech Stack

- **Language**: Kotlin
- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **UI Framework**: Jetpack Compose with Material 3
- **Architecture**: Single Activity + MVVM pattern
- **ViewModel**: StateFlow for reactive state management
- **Navigation**: Navigation Compose
- **Build System**: Groovy build.gradle (not Kotlin DSL)

## Dependencies

- `androidx.navigation:navigation-compose` - Navigation between screens
- `androidx.lifecycle:lifecycle-viewmodel-compose` - ViewModel integration
- `io.coil-kt:coil-compose` - Image loading and display
- `com.android.billingclient:billing-ktx:7.0.0` - Google Play Billing

## Project Structure

```
app/
├── src/main/
│   ├── java/com/zaheer/photocompressor/
│   │   ├── MainActivity.kt                   # Single activity entry point
│   │   ├── billing/
│   │   │   └── BillingManager.kt            # Google Play Billing logic
│   │   ├── domain/
│   │   │   └── ExactKbCompressor.kt         # Compression engine
│   │   ├── ui/
│   │   │   ├── navigation/
│   │   │   │   └── AppNav.kt                # Navigation configuration
│   │   │   ├── screens/
│   │   │   │   ├── HomeScreen.kt            # Main screen
│   │   │   │   └── ResultScreen.kt          # Results display
│   │   │   ├── theme/
│   │   │   │   ├── Color.kt                 # Material 3 colors
│   │   │   │   ├── Theme.kt                 # Theme configuration
│   │   │   │   └── Type.kt                  # Typography
│   │   │   └── viewmodel/
│   │   │       ├── CompressorUiState.kt     # UI state definitions
│   │   │       └── CompressorViewModel.kt   # Business logic
│   │   └── utils/
│   │       ├── FormatUtils.kt               # Display formatting
│   │       ├── ImageDecodeUtils.kt          # Image decoding utilities
│   │       ├── MediaStoreUtils.kt           # Gallery save operations
│   │       └── ShareUtils.kt                # Image sharing via FileProvider
│   ├── res/
│   │   └── xml/
│   │       └── file_paths.xml               # FileProvider configuration
│   └── AndroidManifest.xml
├── build.gradle                              # App module build config
└── proguard-rules.pro                        # ProGuard rules

build.gradle                                  # Root build configuration
settings.gradle                               # Project settings
gradle.properties                             # Gradle properties
```

## How It Works

### Compression Algorithm

1. **Image Loading**: Safely decode bitmap with automatic downsampling for large images
2. **Quality Reduction**: Start at 92% quality and reduce in steps to 30%
3. **Dimension Scaling**: If target not met at minimum quality, scale dimensions by 0.9x iteratively
4. **Result**: Returns compressed data with metadata (size, quality, scale, approximate flag)

### State Management

The app uses MVVM with StateFlow:
- `CompressorViewModel` manages compression operations
- `CompressorUiState` represents different UI states (Idle, ImageSelected, Compressing, Complete, Error, etc.)
- UI observes state changes and updates reactively

### Billing Flow

1. App starts → BillingManager initializes and connects
2. Query product details for `photo_compressor_pro`
3. User clicks "Unlock Pro" → Launch billing flow
4. Handle purchase → Acknowledge if needed → Save pro state
5. "Restore Purchases" queries existing purchases and activates pro status

## Building the Project

1. Open the project in Android Studio
2. Sync Gradle files
3. Build and run on device or emulator

```bash
./gradlew build
```

## Setup Instructions for Billing

To test Google Play Billing:

1. Create the in-app product `photo_compressor_pro` in Google Play Console
2. Upload the app as an internal test track
3. Add test users
4. Test purchase flow with real Google Play environment

For development/testing without actual billing:
- The app gracefully handles billing unavailable scenarios
- Pro status can be tested by modifying SharedPreferences

## Permissions

The app only requires:
- `INTERNET` - For Google Play Billing communication

**No storage permissions needed** - Uses MediaStore API and FileProvider for modern Android compatibility.

## Features by Screen

### Home Screen
- Image picker button
- Selected image preview with original size
- Target size input field
- Compress button
- Billing section (if not pro)
- Unlock Pro / Restore Purchases buttons
- Error handling with user-friendly messages

### Result Screen
- Compressed image preview
- Detailed statistics (original size, compressed size, ratio, quality, scale)
- Approximate result indicator
- Save to Gallery button
- Share Image button
- Compress Again / Pick New Image options

## License

This project is provided as-is for educational and commercial use.

## Package

**Package Name**: `com.zaheer.photocompressor`  
**App Name**: Photo Compressor – KB Size
