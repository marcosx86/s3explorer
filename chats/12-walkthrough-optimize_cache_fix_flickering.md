# Performance Improvements Delivered

The major UI hangs and "No media found" issues have been resolved. Here is a summary of what was accomplished:

## S3Client Caching Strategy
> [!TIP]
> The biggest performance gain comes from caching the `S3Client` to prevent repeated resource-heavy instantiation per thumbnail.

- **`S3ClientManager` Created**: A new thread-safe centralized cache ensures we only create one HTTP connection pool per active AWS profile. 
- **`GetPresignedUrlUseCase` Refactored**: Reuses the cached client. Generating 50 presigned URLs as you scroll no longer creates 50 unmanaged network threads.
- **`S3NetworkDataSource` Updated**: Folder fetching and bucket listings now utilize the cached connection, speeding up folder navigation.

## Fixed MIME Type Resolution Bug
> [!IMPORTANT]
> A critical flaw in Android's `MimeTypeMap.getFileExtensionFromUrl()` caused it to return empty strings for any S3 Object keys containing spaces or URL-unsafe characters.

- **Replaced Android Utility**: Switched to robust Kotlin string extensions `filename.substringAfterLast('.')` in `FileExplorerItems`, `FileExplorerViewModel`, and `MediaViewerViewModel`.
- **Restored Visibility**: Items with spaces in their names are now properly recognized as media types. Thumbnails generate properly and clicking them correctly opens the `MediaViewerScreen` without showing the "No media found" error.

## Memory Optimization
- **Background Filtering**: Moved the array filtering operation in `MediaViewerViewModel` to `Dispatchers.Default` so the UI does not drop frames when opening a folder with over 1000 items.
