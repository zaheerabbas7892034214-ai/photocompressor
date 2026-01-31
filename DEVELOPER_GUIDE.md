# Developer Setup Guide

## Prerequisites

- Android Studio (Arctic Fox or later recommended)
- JDK 17
- Android SDK with API 34 installed
- Git

## Quick Start

### 1. Clone the Repository

```bash
git clone https://github.com/zaheerabbas7892034214-ai/photocompressor.git
cd photocompressor
```

### 2. Open in Android Studio

1. Launch Android Studio
2. Click "Open" and select the project directory
3. Wait for Gradle sync to complete
4. If prompted, accept any SDK/tool updates

### 3. Build the Project

#### Via Android Studio
- Click Build → Make Project (Ctrl+F9 / Cmd+F9)

#### Via Command Line
```bash
./gradlew assembleDebug
```

### 4. Run the App

#### On Physical Device
1. Enable Developer Options and USB Debugging on your device
2. Connect device via USB
3. Click Run (Shift+F10 / Ctrl+R)

#### On Emulator
1. Create an AVD (API 24 or higher)
2. Start the emulator
3. Click Run

## Project Configuration

### Gradle Files

- **Root build.gradle**: Contains Kotlin and AGP versions
- **app/build.gradle**: App-level dependencies and configuration
- **settings.gradle**: Repository and module configuration
- **gradle.properties**: Build optimization settings

### Important Dependencies

```groovy
// Compose BOM - manages Compose versions
implementation platform('androidx.compose:compose-bom:2023.10.01')

// Navigation
implementation 'androidx.navigation:navigation-compose:2.7.5'

// ViewModel
implementation 'androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2'

// Image Loading
implementation 'io.coil-kt:coil-compose:2.5.0'

// Billing
implementation 'com.android.billingclient:billing-ktx:7.0.0'
```

## Testing Google Play Billing

### Development Testing (Without Play Store)

The app will show billing errors but will function for image compression. Pro features can be tested by:

1. Opening the app
2. Navigating to SharedPreferences
3. Setting `is_pro` to `true`

### Production Testing (With Play Store)

1. **Setup in Google Play Console**:
   - Create app in Play Console
   - Navigate to Monetization → In-app products
   - Create product with ID: `photo_compressor_pro`
   - Set type: One-time purchase
   - Set price and save

2. **Create Internal Test Track**:
   - Upload signed APK/AAB
   - Create internal test track
   - Add testers via email

3. **Test on Device**:
   - Tester accepts invitation
   - Downloads app from Play Store
   - Tests purchase flow

## Code Structure Overview

### Layers

1. **UI Layer** (`ui/`)
   - Screens (HomeScreen, ResultScreen)
   - Navigation (AppNav)
   - Theme (Colors, Typography)
   - ViewModel (State management)

2. **Domain Layer** (`domain/`)
   - ExactKbCompressor (Compression algorithm)

3. **Utils Layer** (`utils/`)
   - ImageDecodeUtils (Image processing)
   - MediaStoreUtils (Gallery operations)
   - ShareUtils (File sharing)
   - FormatUtils (Display formatting)

4. **Billing Layer** (`billing/`)
   - BillingManager (Google Play Billing)

### Key Files

- `MainActivity.kt` - Single activity entry point
- `CompressorViewModel.kt` - Business logic and state
- `CompressorUiState.kt` - UI state definitions
- `ExactKbCompressor.kt` - Core compression engine
- `BillingManager.kt` - In-app purchase management

## Common Issues

### Gradle Sync Failed

**Solution**: 
- Check internet connection
- Invalidate Caches (File → Invalidate Caches)
- Clean project: `./gradlew clean`

### Compose Preview Not Working

**Solution**:
- Ensure you're using `@Preview` annotation
- Check that preview functions have no parameters
- Rebuild project

### Billing Connection Failed

**Expected in development**: Billing requires a real Play Store environment. For testing:
- Use internal test track
- Or handle gracefully with error messages

### Image Picker Not Working

**Solution**:
- Test on API 24+ device/emulator
- Ensure system has images available
- Check logcat for permission issues

## Debugging

### Enable Verbose Logging

Add to your module's build.gradle:

```groovy
android {
    buildTypes {
        debug {
            debuggable true
            minifyEnabled false
        }
    }
}
```

### Useful ADB Commands

```bash
# View logs
adb logcat | grep "PhotoCompressor"

# Clear app data
adb shell pm clear com.zaheer.photocompressor

# Check SharedPreferences
adb shell run-as com.zaheer.photocompressor cat /data/data/com.zaheer.photocompressor/shared_prefs/billing_prefs.xml
```

## Contributing

When contributing:
1. Follow existing code style
2. Use meaningful commit messages
3. Test on multiple API levels
4. Update documentation as needed

## Build Variants

### Debug
- Debuggable
- No code minification
- Faster builds

### Release
- Minification enabled
- ProGuard rules applied
- Requires signing configuration

To build release APK:
```bash
./gradlew assembleRelease
```

## Resources

- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
- [Google Play Billing Library](https://developer.android.com/google/play/billing)
- [Material Design 3](https://m3.material.io/)
- [Android Developer Guide](https://developer.android.com/guide)

## Support

For issues or questions:
1. Check existing GitHub issues
2. Review Android documentation
3. Open a new issue with details

## Version Information

- **Kotlin**: 1.9.20
- **Compose**: 1.5.4
- **AGP**: 8.1.4
- **Min SDK**: 24
- **Target SDK**: 34
- **Billing Library**: 7.0.0
