# Video Player Implementation Plan

This plan details the implementation of a dedicated video player activity using Media3 (ExoPlayer) and Jetpack Compose for the custom overlay, as requested.

## Proposed Changes

### 1. Dependencies

#### [MODIFY] `app/build.gradle.kts`
- Add Media3 ExoPlayer and UI dependencies for industry-standard robust media playback.
  - `androidx.media3:media3-exoplayer:1.2.1`
  - `androidx.media3:media3-ui:1.2.1`

### 2. Video Player Component

#### [NEW] `VideoPlayerActivity.kt`
- Create a dedicated `ComponentActivity` for video playback.
- Configure Jetpack Compose immersive mode to hide system bars for a true full-screen experience.
- Implement the video surface using `AndroidView` wrapping a `PlayerView` (with `useController = false` to hide default ExoPlayer controls).
- **Custom Compose Overlay**:
  - **Gestures**: Implement a single tap on the surface to toggle control visibility. Implement a swipe-down gesture using pointer input to `finish()` the activity.
  - **Top Bar**: A back button aligned to the top left.
  - **Bottom Panel**:
    - A custom `Slider` configured with a red track and thumb (dot) to act as the progress bar.
    - Below the slider: Left-aligned timecode `Text` (e.g., `01:23 / 04:56` or `01:05:20 / 02:10:00` for hours).
    - Right-aligned `IconButton` for the fullscreen toggle (changes orientation or layout mode).
- Maintain player state (`isPlaying`, `currentPosition`, `duration`) mapped to Compose State, updating via a polling coroutine while the video is playing.

### 3. Application Manifest

#### [MODIFY] `AndroidManifest.xml`
- Register `VideoPlayerActivity`.
- Apply `android:configChanges="orientation|screenSize"` to handle fullscreen toggling seamlessly without destroying the activity.

### 4. Integration with Media Viewer

#### [MODIFY] `MediaViewerScreen.kt`
- In `MediaPage`, make the `Icon(Icons.Default.PlayArrow)` or its surrounding box clickable for video items.
- On click, invoke an `Intent` to launch `VideoPlayerActivity`, passing the resolved pre-signed URL as an extra string.

## User Review Required

> [!WARNING]
> Since videos might have expired URLs if the viewer is kept open for more than an hour, we are currently passing the pre-signed URL directly to the new Activity. If URLs are strictly 1-hour limited, this is normally fine. If you prefer, we could pass the profile/bucket/objectKey and resolve the URL *inside* the new activity instead. Passing the URL directly is much faster (instant playback). I will proceed with passing the URL directly unless you specify otherwise.

## Verification Plan

### Manual Verification
1. Open the Media Viewer on a video file.
2. Tap the central Play button.
3. Verify `VideoPlayerActivity` opens and begins buffering/playing the video.
4. Verify a single tap toggles the custom red UI controls.
5. Validate the timecode format and the custom red progress bar.
6. Swipe down to verify the gesture successfully closes the player and returns to the viewer.
