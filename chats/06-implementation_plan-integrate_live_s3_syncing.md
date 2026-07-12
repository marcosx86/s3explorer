# S3 Explorer UI: Phase 5 Implementation Plan (Live S3 Sync)

This plan details replacing our mock synchronization with a real network fetch using the AWS SDK Kotlin. We will download the actual contents of the S3 bucket and insert them into the Room database to complete the Single Source of Truth architecture.

## User Review Required

> [!IMPORTANT]
> **Pagination Scope:** S3's `listObjectsV2` returns up to 1,000 keys per request. For this initial live integration, I propose we fetch the first page (up to 1,000 items) for a given folder and sync it to Room. Advanced Paging 3 `RemoteMediator` logic (which automatically requests the next 1,000 items from S3 as the user scrolls past item 900 in the UI) requires a complex state machine and will be handled in a polish phase. Is this acceptable?

## Open Questions

- When the `SyncDirectoryUseCase` wipes the local cache for a specific folder to refresh it, what should the UX be? Should it silently update in the background, or should it block the UI with a spinner? (I recommend silent background update, with a small refreshing indicator at the top).

## Proposed Changes

### 1. Connection Profile Routing
We currently navigate using only the `bucketName`. However, the `FileExplorerViewModel` needs the *credentials* to fetch files from S3.
- **[MODIFY]** `SaveConnectionProfileUseCase.kt`: Update to return the generated `profileId` (String).
- **[MODIFY]** `NewConnectionViewModel.kt` & `NewConnectionScreen.kt`: Expose the `profileId` on successful connection.
- **[MODIFY]** `S3Navigation.kt`: Update the route to `file_explorer/{profileId}/{bucketName}`.

### 2. Network Data Layer (`data/remote/`)
- **[MODIFY]** `S3NetworkDataSource.kt`: Add a new method `listObjects(endpoint, access, secret, bucket, prefix)`.
  - This method will construct a `ListObjectsV2Request` with `delimiter = "/"` to group files into folders (CommonPrefixes).
  - It will return a custom data class containing both folders and files.

### 3. Domain & Sync Layer (`domain/`)
- **[MODIFY]** `SyncDirectoryUseCase.kt`:
  - It will now accept the `profileId`.
  - Fetch the endpoint and decrypted secret key from `ConnectionRepository`.
  - Call `S3NetworkDataSource.listObjects()`.
  - Transform the S3 response into `S3ObjectEntity` rows.
  - Clear the old Room cache for this prefix, and insert the new live rows.

### 4. UI Layer (`ui/explorer/`)
- **[MODIFY]** `FileExplorerViewModel.kt`: Retrieve `profileId` from `SavedStateHandle` and pass it to `SyncDirectoryUseCase`.

## Verification Plan

### Manual Verification
- Deploy to the emulator.
- Connect to your real local MinIO instance (or an AWS S3 bucket).
- Navigate to the File Explorer.
- The UI should instantly display a loading state, followed by the real folders and files pulled directly from your bucket!
- Navigating into a folder will trigger another live fetch for that specific prefix.
