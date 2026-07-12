# Performance Improvements & Bug Fixes

This plan outlines the solutions to the massive UI hanging, delayed thumbnail loading, and the "No media found" errors on slow devices.

## Background Context
During investigation, two critical issues were discovered:
1. **Resource Leak in URL Presigning**: The `GetPresignedUrlUseCase` creates a new `S3Client` (and internally a new HTTP engine with thread pools) for *every single thumbnail* shown on screen. Crucially, these clients are never closed. When scrolling through a list of 1000 items, Paging triggers dozens of concurrent requests, spinning up massive amounts of thread pools that exhaust device memory and CPU, causing the UI to hang indefinitely.
2. **"No Media Found" Bug**: Android's `MimeTypeMap.getFileExtensionFromUrl()` fails and returns empty strings when file names contain spaces or special characters. Because of this, the app fails to recognize these files as images or videos, skips thumbnail generation, and filters them out entirely in the `MediaViewerViewModel`. Clicking on one of these items results in the empty viewer screen.

## Proposed Changes

### 1. Centralized S3Client Management
To prevent resource leaks and instantiation overhead, we will introduce a centralized cache for `S3Client` instances.

#### [NEW] `app/src/main/java/net/m21xx/s3explorer/data/remote/S3ClientManager.kt`
- Create a `@Singleton` class that holds a thread-safe `ConcurrentHashMap<String, S3Client>`.
- Add a method to `getClient(profileId, connectionRepository, connectionProfileDao)` that reuses the existing client or creates one if it doesn't exist.
- Add an `invalidateClient(profileId)` method for when users update their credentials.

### 2. Update Use Cases and Data Sources
#### [MODIFY] `app/src/main/java/net/m21xx/s3explorer/domain/GetPresignedUrlUseCase.kt`
- Inject `S3ClientManager`.
- Remove the local instantiation of `S3Client`.
- Use the cached client to generate presigned URLs instantly without resource leaks.

#### [MODIFY] `app/src/main/java/net/m21xx/s3explorer/data/remote/S3NetworkDataSource.kt`
- Inject `S3ClientManager` and use it instead of recreating and destroying `S3Client` on every network operation. This will speed up folder navigation significantly since HTTP connections can be pooled and reused.

### 3. Fix MIME Type Detection (The "No Media Found" Fix)
#### [MODIFY] `app/src/main/java/net/m21xx/s3explorer/ui/explorer/FileExplorerItems.kt`
- Replace `MimeTypeMap.getFileExtensionFromUrl(item.objectKey)` with reliable string manipulation:
  ```kotlin
  val filename = item.objectKey.substringAfterLast('/')
  val extension = if (filename.contains('.')) filename.substringAfterLast('.').lowercase() else ""
  ```

#### [MODIFY] `app/src/main/java/net/m21xx/s3explorer/ui/viewer/MediaViewerViewModel.kt`
- Apply the same robust extension extraction logic.
- Move the list mapping operation (`mediaItems = allFiles.mapNotNull { ... }`) to `Dispatchers.Default` to prevent any frame drops on the Main thread when parsing folders with 1000+ files.

## Verification Plan

### Manual Verification
- Deploy to the Moto Z2 Play or Emulator.
- Open a folder with 1000 images and 200 videos.
- Verify that scrolling no longer hangs the app and thumbnails load responsively.
- Click on an item with spaces in its name; verify the `MediaViewerScreen` opens and displays the image properly without "No media found".
