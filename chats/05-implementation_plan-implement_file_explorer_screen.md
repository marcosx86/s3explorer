# S3 Explorer UI: Phase 4 Implementation Plan (File Explorer)

This plan outlines the architecture for the main File Explorer screen, adhering to our Single Source of Truth (SSOT) pattern utilizing Room and Paging 3.

## User Review Required

> [!IMPORTANT]
> **SSOT and Network Syncing:** For this initial phase, we will implement the UI and the Local Database layer (Room) with Paging 3. We will mock the synchronization process that populates the Room DB to ensure the UI handles pagination, sorting, and empty states perfectly before we hook up the live `aws-sdk-kotlin` pagination logic in a subsequent step. 

## Open Questions

- Should the Bottom Navigation (Files, Recent, Offline) be implemented as dummy tabs for now, or just focus strictly on the Top App Bar and the main list content?

## Proposed Changes

### 1. Data Layer (Room Cache)
- **[NEW]** `S3ObjectEntity.kt`: Room entity representing a file or folder. Fields: `objectKey` (PK), `bucketName`, `size`, `lastModified`, `isDirectory`.
- **[NEW]** `S3ObjectDao.kt`: DAO returning a `PagingSource<Int, S3ObjectEntity>` for efficient chunked loading into the UI.
- **[MODIFY]** `AppDatabase.kt`: Register `S3ObjectEntity` and `S3ObjectDao`.

### 2. Domain & Sync Layer (Use Cases)
- **[NEW]** `ObserveDirectoryContentUseCase.kt`: Exposes a `Pager` flow from the `S3ObjectDao` to the ViewModel.
- **[NEW]** `SyncDirectoryUseCase.kt`: For now, this will generate mock `S3ObjectEntity` data (including folders, files, and images) and insert them into Room to simulate a network fetch.

### 3. UI State & ViewModel (`ui/explorer/`)
- **[NEW]** `FileExplorerViewModel.kt`: State holder that triggers `SyncDirectoryUseCase` and exposes the `PagingData<S3ObjectEntity>` flow to the UI.
- **[NEW]** `FileExplorerState.kt`: Holds current bucket, prefix, and loading states.

### 4. UI Layout (`ui/explorer/`)
- **[NEW]** `FileExplorerScreen.kt`: The main screen.
  - **Top App Bar**: Hamburger menu, title, '+' action button.
  - **Bottom Navigation**: Tabs for Files, Recent, Offline.
  - **Main Content**: A `LazyColumn` powered by `collectAsLazyPagingItems()`.
- **[NEW]** `components/FileExplorerItems.kt`: Composable rows for:
  - Empty State (Folder icon + "Tap + to add files here").
  - Folder Item.
  - File Item (with formatted size/date).
  - Media Item (prepared to use Coil for thumbnails).

### 5. Navigation Integration
- **[MODIFY]** `S3Navigation.kt`: Update the `FILE_EXPLORER` route to properly host `FileExplorerScreen` and accept the selected bucket name as a navigation argument.

## Verification Plan

### Automated Tests
- N/A for this UI-heavy phase.

### Manual Verification
- Deploy to the emulator.
- Successfully connect from the New Connection screen.
- Verify the File Explorer loads, the Paging 3 list correctly fetches mock data from Room, and scrolling is perfectly smooth.
- Verify the Empty State displays correctly if the database is empty.
