# UI and Application Settings Implementation Plan

This plan details the steps required to implement the features outlined in `specs/19-ui-and-app-settings.md`. 

## Open Questions
> [!IMPORTANT]
> 1. **Biometric Authentication:** Should the secure app lock use Android's BiometricPrompt (fingerprint/face)? This will require adding the `androidx.biometric` dependency.
> 2. **PIX QR Code:** Do you have the actual PIX key/QR code image for the About screen, or should I use a placeholder for now?
> 3. **App Version:** Should I dynamically read the `versionName` from `PackageManager` to display in the About screen?

## Proposed Changes

---

### Data Layer: DataStores

#### [MODIFY] [SettingsDataStore.kt](file:///c:/git/s3explorer/app/src/main/java/net/m21xx/s3explorer/data/local/preferences/SettingsDataStore.kt)
- Add new properties to `GlobalPreferences`:
  - `themeMode`: String (Light, Dark, System) - Default: System
  - `showVideoThumbnails`: Boolean - Default: true
  - `customUserAgent`: String - Default: "S3Explorer/1.0 (Android)"
- Expose methods to update these properties.

#### [MODIFY] [ProfilePreferencesDataStore.kt](file:///c:/git/s3explorer/app/src/main/java/net/m21xx/s3explorer/data/local/preferences/ProfilePreferencesDataStore.kt)
- Add new properties to `ProfilePreferences`:
  - `storageClass`: String - Default: "" (empty)
  - `skipSameFileUpload`: Boolean - Default: false
  - `multipartConcurrentParts`: Int - Default: 5
  - `multipartChunkSizeMB`: Int - Default: 10
  - `multipartStartThresholdMB`: Int - Default: 150 (replace the old scalar threshold)
  - `generateThumbnailsLocally`: Boolean - Default: true
  - `uploadThumbnailsRemotely`: Boolean - Default: false
  - `uploadTimeoutMs`: Long - Default: 300000
  - `downloadTimeoutMs`: Long - Default: 300000
- Expose methods to update these properties.

---

### Core UI Logic: Activity & Theming

#### [MODIFY] [MainActivity.kt](file:///c:/git/s3explorer/app/src/main/java/net/m21xx/s3explorer/MainActivity.kt)
- Implement a 1500ms delay on the Splash Screen using `installSplashScreen().setKeepOnScreenCondition`.
- Inject `SettingsDataStore` to observe `themeMode` and dynamically apply the correct `darkTheme` parameter to `S3ExplorerTheme`.
- Implement `BiometricPrompt` on `onResume()` if `enableLockScreen` is true in `GlobalPreferences`, preventing access until authenticated.

#### [MODIFY] [Theme.kt](file:///c:/git/s3explorer/app/src/main/java/net/m21xx/s3explorer/ui/theme/Theme.kt)
- Ensure `S3ExplorerTheme` respects the explicitly passed `darkTheme` parameter rather than only relying on `isSystemInDarkTheme()`.

---

### UI Components: Screens & Settings

#### [MODIFY] [GlobalSettingsScreen.kt](file:///c:/git/s3explorer/app/src/main/java/net/m21xx/s3explorer/ui/settings/GlobalSettingsScreen.kt)
- Add "Choose theme" preference row (opens a dialog or dropdown to select Light/Dark/System).
- Add "Show video thumbnails" explicit toggle alongside the image thumbnail toggle.
- Add "Custom user agent" preference row (opens an input dialog to modify the UA string).

#### [MODIFY] [AccountSettingsScreen.kt](file:///c:/git/s3explorer/app/src/main/java/net/m21xx/s3explorer/ui/settings/AccountSettingsScreen.kt)
- Expand with section titles (Privacy, Bucket, Upload, Thumbnails, Cleanup, Network).
- Build Modal dialogs with input fields for:
  - Passphrase for E2E encryption.
  - Storage Class.
  - Upload/Download Timeout in ms.
- Build Slider components for:
  - Upload concurrency.
  - Multipart concurrent parts, Chunk size, and Start threshold.
- Build actions for Cleanup: Clear document cache, Clear local thumbnail cache, Delete pending multipart uploads.

#### [NEW] `app/src/main/java/net/m21xx/s3explorer/ui/settings/AboutScreen.kt`
- Create a dedicated "About" screen (accessible via navigation router).
- Header: App logo and "Lorem ipsum" description.
- Donation section: QR code image for PIX. Tapping copies the PIX key to the clipboard.
- Footer: Developer's GitHub profile link and dynamically fetched app version centered at the bottom.

#### [MODIFY] [S3Navigation.kt](file:///c:/git/s3explorer/app/src/main/java/net/m21xx/s3explorer/ui/navigation/S3Navigation.kt)
- Register `AboutScreen` as a new destination in the `S3NavHost`.

## Verification Plan

### Automated Tests
- The project's compilation will be verified with `./gradlew assembleDebug` to ensure all new DataStore preferences and UI composables compile successfully.

### Manual Verification
- Review changes to ensure settings accurately reflect in DataStore via UI updates.
- Ensure the Splashscreen holds for 1500ms before revealing the app.
- Ensure the "About" screen renders correctly and the PIX key copies to the clipboard.
