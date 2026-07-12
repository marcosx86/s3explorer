# Async Thumbnail Loading (Images & Videos) Implementation Plan

This plan details how we will achieve highly-efficient async thumbnail loading from S3, strict concurrency limits (max 5), native `MimeTypeMap` media detection, and video frame extraction using Coil's video extension.

## Proposed Architecture

### 1. Dependencies Update
To support video frame extraction, we need to add Coil's official video extension.

#### [MODIFY] [build.gradle.kts (app)](file:///C:/git/s3explorer/app/build.gradle.kts)
- Add `implementation("io.coil-kt:coil-video:2.5.0")` under Coil dependency block.

---

### 2. Concurrency Limiting & Video Decoder Registration

We will configure Coil's global `ImageLoader` in the `Application` class to:
- Use a dedicated `OkHttpClient` with a custom `Dispatcher` capped at `maxRequests = 5` and `maxRequestsPerHost = 5`.
- Register the `VideoFrameDecoder.Factory` so Coil can automatically extract the first frame from remote video URLs.

#### [MODIFY] [S3ExplorerApp.kt](file:///C:/git/s3explorer/app/src/main/java/net/m21xx/s3explorer/S3ExplorerApp.kt)
- Make the `Application` class implement `ImageLoaderFactory`.
- Construct the custom `OkHttpClient`.
- Initialize `ImageLoader` registering `VideoFrameDecoder.Factory()` in its components block.

---

### 3. Media Type Detection & Presigned URL Generation

#### [NEW] [GetPresignedUrlUseCase.kt](file:///C:/git/s3explorer/app/src/main/java/net/m21xx/s3explorer/domain/GetPresignedUrlUseCase.kt)
- Injects `ConnectionProfileDao` and `ConnectionRepository` to retrieve secure credentials.
- Creates an ephemeral `S3Client` and calls `GetObjectRequest { ... }.presignGetObject(...)` to return a URL valid for 1 hour.

#### [MODIFY] [FileExplorerViewModel.kt](file:///C:/git/s3explorer/app/src/main/java/net/m21xx/s3explorer/ui/explorer/FileExplorerViewModel.kt)
- Expose a `suspend fun getThumbnailUrl(item: S3ObjectEntity): String?`.
- Uses Android's `MimeTypeMap` to query the mime type of the object extension.
- If the MIME type starts with `"image/"` or `"video/"`, calls `GetPresignedUrlUseCase` to return the signed URL. Otherwise, returns `null` immediately.

---

### 4. UI Layer Integration

#### [MODIFY] [FileExplorerScreen.kt](file:///C:/git/s3explorer/app/src/main/java/net/m21xx/s3explorer/ui/explorer/FileExplorerScreen.kt)
- Pass a `getThumbnailUrl = { viewModel.getThumbnailUrl(it) }` lambda to the items.

#### [MODIFY] [FileExplorerItems.kt](file:///C:/git/s3explorer/app/src/main/java/net/m21xx/s3explorer/ui/explorer/FileExplorerItems.kt)
- For `DetailedListItem`, `CompactListItem`, and `GalleryCardItem`:
  - Launch a coroutine using `LaunchedEffect` to fetch the presigned URL.
  - Bind the resulting URL to `AsyncImage(model = url)`.
  - Maintain the existing `crossfade(true)` and `size()` limits.
  - For `GalleryFolderCardItem` and folders in list views, continue using the static vector icon immediately.

## Verification Plan
1. Upload images (PNG, WebP, JPG) and videos (MP4, MKV) to an S3 bucket.
2. Navigate to that bucket in the File Explorer.
3. Observe images and video frames gracefully fading in (max 5 at a time) when in Gallery or List mode.
4. Verify non-supported file formats (like `.txt`, `.pdf`) do not request presigned URLs and display vector placeholder icons instantly.
