# Media Viewer Implementation Plan

This plan details the implementation of a high-performance, gesture-driven media slider for `image/*` and `video/*` media types, natively built in Jetpack Compose, as requested in `docs/16-media-viewer.md`.

## User Review Required

> [!IMPORTANT]  
> **Database Schema & Filtering:** Currently, `S3ObjectEntity` does not store a `mimeType`. To avoid a database migration, I propose that the `MediaViewerViewModel` queries all non-directory objects for the given `parentPrefix` from Room and filters them in-memory using `MimeTypeMap` to create the final list of media items. This is perfectly safe for typical folder sizes and keeps the implementation simple. Let me know if you prefer to perform a Room database migration to add a `mimeType` column instead.

> [!WARNING]  
> **Videos:** As requested, videos will display their thumbnail (extracted previously) but will NOT support zoom or double-tap gestures. They are treated as WIP placeholders for a future video player sub-feature.

## Open Questions
- None at this time.

## Proposed Changes

### Navigation Layer

#### [MODIFY] [S3Navigation.kt](file:///c:/git/s3explorer/app/src/main/java/net/m21xx/s3explorer/ui/navigation/S3Navigation.kt)
- Add a new destination: `Destinations.MEDIA_VIEWER`.
- Route: `media_viewer/{profileId}/{bucketName}?parentPrefix={parentPrefix}&initialObjectKey={initialObjectKey}`
- Add the corresponding `composable` block that instantiates `MediaViewerScreen`.

---

### Data & State Layer

#### [MODIFY] [S3ObjectDao.kt](file:///c:/git/s3explorer/app/src/main/java/net/m21xx/s3explorer/data/local/dao/S3ObjectDao.kt)
- Add a non-paged query to fetch all files in a folder for the slider to calculate the total size and indices:
  ```kotlin
  @Query("SELECT * FROM s3_objects WHERE profileId = :profileId AND bucketName = :bucketName AND parentPrefix = :parentPrefix AND isDirectory = 0 ORDER BY objectKey ASC")
  suspend fun getAllFilesByPrefix(profileId: String, bucketName: String, parentPrefix: String): List<S3ObjectEntity>
  ```

#### [NEW] [MediaViewerViewModel.kt](file:///c:/git/s3explorer/app/src/main/java/net/m21xx/s3explorer/ui/viewer/MediaViewerViewModel.kt)
- Fetch all files using the new DAO method.
- Filter the list in-memory using `MimeTypeMap` to keep only `image/*` and `video/*`.
- Find the `initialPage` by searching for `initialObjectKey` in the filtered list.
- Expose `uiState: StateFlow<MediaViewerState>`.
- Expose a `suspend fun getPresignedUrl(objectKey: String)` method that utilizes the existing `GetPresignedUrlUseCase`. This allows for prefetching adjacent items as the user swipes.

---

### UI Layer

#### [NEW] [MediaViewerScreen.kt](file:///c:/git/s3explorer/app/src/main/java/net/m21xx/s3explorer/ui/viewer/MediaViewerScreen.kt)
- Use `androidx.compose.foundation.pager.HorizontalPager` as the core component.
- The screen will be a full-screen Compose view with a pure black background.
- Hide the top app bar for an immersive experience (or overlay a translucent top bar with a back button).

#### [NEW] [ZoomableMediaBox.kt](file:///c:/git/s3explorer/app/src/main/java/net/m21xx/s3explorer/ui/viewer/ZoomableMediaBox.kt)
- A reusable composable that encapsulates the gesture math.
- For images:
  - Implement `Modifier.pointerInput(Unit)` with `detectTransformGestures` for pinch-to-zoom and panning.
  - Implement `detectTapGestures` for double-tap zoom (toggles between `1f` and `4f` scale).
  - Use `graphicsLayer` to apply scale and translation.
  - Carefully handle boundary detection so that when zoomed out (`scale == 1f`) or panned to the edge, the unconsumed gestures fall through to the `HorizontalPager` to allow swiping to the next image.
- For videos:
  - Just display the image using Coil with `ContentScale.Fit`.
  - Disable zoom and double-tap gestures.
  - Show a visual indicator (like a "play" icon overlay) to signify it's a video.

#### [MODIFY] [FileExplorerScreen.kt](file:///c:/git/s3explorer/app/src/main/java/net/m21xx/s3explorer/ui/explorer/FileExplorerScreen.kt)
- In the `FileExplorerItems`, add a click listener to all media items.
- When clicked, trigger a navigation callback `onMediaItemClick(objectKey)` that routes to the `MediaViewerScreen`.

## Verification Plan
### Manual Verification
- Tap an image in the File Explorer.
- Verify the Media Viewer opens full-screen to the correct image.
- Swipe left and right to verify the pager works.
- Pinch-to-zoom on an image. Verify panning works while zoomed.
- Reach the edge of a zoomed image and continue swiping; verify the pager snaps to the next image.
- Double-tap an image to verify 4x zoom toggle.
- Verify videos load but do not respond to zoom or double-tap gestures.
