# Splash Screen & Brand Identity Implementation Plan

This plan details the process for extracting the brand assets from `logo_guide.png`, integrating the Android 12+ Splash Screen, reworking the Material 3 Theme palette, and adding the thematic watermark.

## Proposed Changes

### 1. Asset Extraction (`assets/` -> `res/drawable`)
- **Action**: Write a Python script (`extract_assets.py`) to programmatically crop the `logo_guide.png` image.
  - **Crop 1 (1024dp Square Icon)**: Extracted and saved as the base high-res logo.
  - **Crop 2 (Splash Screen Background)**: Extracted to sample the top-most "Soft blue gradient" color (`windowSplashScreenBackground`).
  - **Transparent Foreground**: Use the `rembg` ML library (or similar background removal technique) to strip the blue background from the square icon, isolating just the boy and bucket. This will be saved as `ic_logo_transparent.png`.

### 2. Splash Screen Integration
#### [MODIFY] `app/build.gradle.kts`
- Add `implementation("androidx.core:core-splashscreen:1.0.1")`.

#### [NEW] `app/src/main/res/values/splash_theme.xml`
- Create a dedicated launch theme `Theme.App.Starting` inheriting from `Theme.SplashScreen`.
- Set `windowSplashScreenBackground` to the sampled sky blue.
- Set `windowSplashScreenAnimatedIcon` to `ic_logo_transparent.png`.
- Ensure safe margins by sizing the icon appropriately within the 80% safe zone.

#### [MODIFY] `AndroidManifest.xml` & `MainActivity.kt`
- Apply `Theme.App.Starting` to `MainActivity`.
- Call `installSplashScreen()` in `MainActivity.onCreate()` before `setContent`.

### 3. Material 3 Theme Palette Rework
#### [MODIFY] `app/src/main/java/net/m21xx/s3explorer/ui/theme/Color.kt`
- **Primary**: Deep Ocean Blue (sampled from the water at the base of the bucket).
- **Secondary**: Soft Sky Blue (sampled from the sky background).
- **Tertiary**: Warm Wood/Brass (sampled from the telescope/bucket handle).
- **Dark Surface**: Deeply desaturated Navy Blue / Slate Gray.
- **Light Surface**: Crisp white / cool-tinted off-white.

#### [MODIFY] `app/src/main/java/net/m21xx/s3explorer/ui/theme/Theme.kt`
- Update `LightColorScheme` and `DarkColorScheme` using the newly defined colors to create a cohesive brand experience.

### 4. Thematic Watermark UI Element
#### [NEW] `app/src/main/java/net/m21xx/s3explorer/ui/components/WatermarkBackground.kt`
- Create a reusable Compose `Image` component displaying `ic_logo_transparent.png`.
- **Styling**:
  - `Modifier.fillMaxSize()` with a scaled-up size, slightly offset to the bottom right.
  - Apply `alpha = 0.05f`.
  - Apply `ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })` for dynamic grayscale desaturation.
  - Apply `BlendMode.Multiply` in Light Mode, and standard alpha blending in Dark Mode.

#### [MODIFY] `app/src/main/java/net/m21xx/s3explorer/ui/explorer/FileExplorerScreen.kt`
- Inject the `WatermarkBackground` behind the "Empty Directory" or "Empty Trash" states so it acts as a subtle texture.

## User Review Required

> [!IMPORTANT]
> **Asset Extraction Approach**
> I will install Python's `Pillow` and `rembg` libraries via a background task to dynamically slice your `logo_guide.png` and remove the background from the central logo to create the isolated boy/bucket foreground. This keeps your APK lean without relying on you to manually provide a transparent PNG.

## Verification Plan
1. **App Launch**: Verify the Android 12+ splash screen appears with the soft blue background and the transparent logo securely inside the safe margins.
2. **Theme UI**: Navigate through the app and verify the Primary, Secondary, and Tertiary colors apply correctly to buttons, app bars, and backgrounds across both Light and Dark modes.
3. **Empty States**: Create an empty bucket or folder and verify the large, grayscale, highly-transparent watermark appears dynamically correctly blended into the background.
