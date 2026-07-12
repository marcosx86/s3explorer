# S3 Explorer: Phase 3 Implementation Plan (Network Integration)

This plan outlines replacing our mock network delay with real S3 communication using the `aws-sdk-kotlin`. We will implement the ability to dynamically test connections and list available buckets directly from the "New Connection" screen.

## User Review Required

> [!IMPORTANT]
> **S3-Compatible Storage (MinIO, etc.):** To ensure compatibility with non-AWS endpoints like MinIO or Ceph, the SDK must be configured with `forcePathStyle = true`. I will also use a dummy region (e.g., `us-east-1`) since custom endpoints often require a region string even if they don't strictly enforce AWS regions.

## Open Questions

- For the "bucket selection modal" triggered by the `≡` icon on the Bucket Name field, I plan to use a Material 3 `ModalBottomSheet`. Does this align with your vision for the UI?

## Proposed Changes

### 1. Network Data Layer (`data/remote/`)
- **[NEW]** `S3NetworkDataSource.kt`: This class will handle dynamic instantiation of the `S3Client`. 
  - It will use `StaticCredentialsProvider` with the user-provided Access Key and Secret Key.
  - It will expose a method `listBuckets()` which maps the SDK's response to a simple `List<String>`.

### 2. Domain Layer (`domain/`)
- **[NEW]** `FetchAvailableBucketsUseCase.kt`: Takes the raw endpoint and credentials from the UI, invokes `S3NetworkDataSource.listBuckets()`, and handles any SDK-specific exceptions, returning a standard Kotlin `Result<List<String>>`.

### 3. UI State & ViewModel (`ui/connection/`)
- **[MODIFY]** `NewConnectionState.kt`: Add fields for `availableBuckets: List<String> = emptyList()`, `isFetchingBuckets: Boolean = false`, and a dedicated error message for bucket fetching.
- **[MODIFY]** `NewConnectionViewModel.kt`: 
  - Add `fetchBuckets()` which triggers the new use case and updates the state.
  - Replace the 1.5s mock delay in `testConnection()` with a real call to `fetchBuckets()`. If the SDK successfully returns a list (even an empty one), the connection is valid, and we proceed to save the profile.

### 4. UI Layout (`ui/connection/`)
- **[MODIFY]** `NewConnectionScreen.kt`:
  - Wire up the `≡` (List) icon in the Bucket Name field to trigger `viewModel.fetchBuckets()`.
  - Add a `ModalBottomSheet` to display the list of fetched buckets. When a user taps a bucket in the modal, it populates the Bucket Name field and dismisses the sheet.
  - Add loading indicators (CircularProgressIndicator) inside the modal while fetching.

## Verification Plan

### Automated Tests
- We will rely on manual verification for the network layer in this phase, as mocking the complex `aws-sdk-kotlin` client for UI tests requires significant boilerplate. We will ensure the UI states (loading, success, error) are testable.

### Manual Verification
- Deploy the app to the emulator.
- Enter a valid MinIO/S3 endpoint and credentials.
- Tap the `≡` icon. Verify the bottom sheet appears, shows a loading spinner, and then populates with the actual buckets from the server.
- Tap a bucket in the list and verify the input field is populated.
- Tap 'Connect' and ensure it only succeeds if the credentials are truly valid against the real endpoint.
