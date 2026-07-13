# Bucket Totalization Implementation Plan

This plan details the implementation of full iterative bucket crawling to calculate accurate storage statistics (total size and object count) for the Connection Drawer.

## Proposed Changes

### 1. UI State Layer

#### [MODIFY] `FileExplorerState.kt`
- Add a new property `val isCalculatingStorageStats: Boolean = false` to `DrawerUIState` so we can show a loading indicator while the background calculation runs.

#### [MODIFY] `FileExplorerViewModel.kt`
- Update `refreshStorageStats()` to:
  - Set `isCalculatingStorageStats = true` when starting the calculation.
  - Launch the calculation in `viewModelScope`.
  - Handle potential network exceptions gracefully (e.g., displaying a snackbar or error message).
  - Set `isCalculatingStorageStats = false` and update the `storageStats` upon completion.

### 2. Network Layer

#### [MODIFY] `S3NetworkDataSource.kt`
- Implement a new `suspend fun calculateTotalStats(...)` method.
- This method will iteratively call `listObjectsV2` **without** a delimiter to traverse the entire bucket.
- Use a `do-while` loop to paginate using `nextContinuationToken` until `isTruncated == false`, accumulating `sizeBytes` and `objectCount` in the process.

### 3. Domain Layer

#### [MODIFY] `CalculateStorageStatsUseCase.kt`
- Change constructor injection from `S3ObjectDao` to `ConnectionProfileDao`, `ConnectionRepository`, and `S3NetworkDataSource`.
- Remove the mocked "710 MB" placeholder implementation.
- Fetch the profile and decrypt the secret key using the repository.
- Invoke the new `calculateTotalStats` network method and return an accurate `StorageStatsSummary` with `System.currentTimeMillis()`.

### 4. UI Components

#### [MODIFY] `ConnectionDrawer.kt`
- Update `DrawerFooter` to observe `drawerState.isCalculatingStorageStats`.
- Display a small `CircularProgressIndicator` in place of the `Refresh` icon when calculation is in progress to give user feedback.

## User Review Required

> [!WARNING]
> Iteratively fetching millions of objects in extremely large buckets can take a long time and consume bandwidth. This implementation uses a `viewModelScope.launch` which means navigating away from the `FileExplorerScreen` might cancel the job. 
> For now, this is acceptable for manual refreshes. Should we consider running this inside a `WorkManager` worker in the future if users want automatic background totalization?

## Verification Plan

### Manual Verification
- Tap the refresh icon on the storage stats in the drawer.
- Ensure the refresh icon turns into a loading spinner.
- Validate that the accurate sum of file sizes and item counts appears upon completion.
- Rotate the device to ensure the loading state is maintained if still executing.
