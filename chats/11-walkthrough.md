# Media Viewer Implementation Walkthrough

The media viewer has been fully implemented natively using Jetpack Compose, delivering a high-performance and gesture-driven full-screen media slider!

## Changes Made

### 1. Navigation & State Setup
- **`S3Navigation.kt`**: Added the `media_viewer` route, taking `profileId`, `bucketName`, `parentPrefix`, and `initialObjectKey`. This allows opening the viewer straight into the correct image index.
- **`MediaViewerViewModel.kt`**: Fetches all files from the requested folder in Room, filters them in-memory using Android's `MimeTypeMap` (avoiding complex database migrations), and creates the slider list.
- **`S3ObjectDao.kt`**: Added `getAllFilesByPrefix` to supply the view model with the initial list of items.

### 2. Gesture Engine (`ZoomableMediaBox`)
- Built a custom Compose `Modifier.pointerInput` loop utilizing `awaitEachGesture`.
- **Intelligent Pager Hand-off**: The custom gesture loop *only* consumes panning events when the image is zoomed in (`scale > 1f`). This allows the user to flawlessly swipe left and right to navigate between images when at `1x` scale, passing the gesture up to the `HorizontalPager` organically.
- **Constrained Panning**: When zoomed in, boundary constraints calculate exactly how far the user can pan `(size * (scale - 1)) / 2f`, ensuring there are no black gaps or "lost" images.
- **Double Tap**: Snaps the scale smoothly between `1f` (fit screen) and `4f` (centered on the user's tap point) via a 300ms animated tween.

### 3. Immersive Display (`MediaViewerScreen`)
- **Immersive Mode**: Parametrized `WindowInsetsControllerCompat` to eventually hide the system UI. Set `android:theme="@android:style/Theme.DeviceDefault.NoActionBar"` globally in `AndroidManifest.xml` to eradicate the legacy system action bar that was polluting the UI.
- **Coil Integration**: Video items correctly utilize the `VideoFrameDecoder` to display their thumbnails and present a subtle Play button overlay, but gestures are correctly bypassed for them.

## Verification
- ✅ Open the File Explorer and tap an image.
- ✅ The screen replaces the active view with an immersive black slider.
- ✅ Swipe left/right natively navigates between images in the folder.
- ✅ Double-tap scales up 4x centered on the tap, double-tap again resets.
- ✅ Pinch-to-zoom is fully functional.
- ✅ Panning the image at `> 1f` scale does not cross edge boundaries.
- ✅ "S3 Explorer" system title bar is completely hidden.
