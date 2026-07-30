# Object Actions and Transfers Implementation Plan

This plan details the implementation of the object context actions (the three-dots menu) and the new interactive Transfers Drawer as specified in `specs/22-object-actions-and-transfers.md`.

## User Review Required
> [!IMPORTANT]
> The introduction of an interactive Transfers Drawer implies that we need a way to track the progress of ongoing uploads and downloads. Currently, `UploadObjectUseCase` executes as a single blocking suspending function without emitting progress. To support the Transfers Drawer, we will need to refactor our network layer and use cases to emit progress updates (e.g., via Kotlin Flows).

## Open Questions

> [!WARNING]
> Please provide your feedback on the following design decisions before we proceed:
> 1. **Sharing Folders**: S3 does not natively support pre-signed URLs for prefixes (folders). When the user taps "Share" on a folder, how should this behave? Should we exclude "Share" from folder actions, or should we zip its contents dynamically?
> 2. **Transfer Manager Scope**: To ensure transfers continue while navigating the app, should we implement a Singleton `TransferManager` (lives as long as the app process) or a full Android Foreground `Service` / `WorkManager` (can run even if the app is backgrounded)? For a simple interactive drawer, a Singleton is usually easiest.
> 3. **Transfers Drawer Navigation**: The spec states the Transfers Drawer "slides in from the bottom" and is "accessible via the connection drawer". Currently, clicking "Transfers" in the connection drawer navigates to a placeholder screen via `NavHost`. Should we remove that screen route entirely and instead open a `ModalBottomSheet` over the `FileExplorerScreen`?

## Proposed Changes

### 1. Object Context Actions (UI Components)

#### [NEW] `net.m21xx.s3explorer.ui.explorer.components.ObjectActionBottomSheet.kt`
- Create a shared `ModalBottomSheet` component that takes an `S3ObjectEntity` (file or folder).
- Implement the UI header displaying the object name, left-aligned.
- Implement the contextual list items (ListItems with Icons) based on whether it's a file or folder:
  - **Files**: Open with, Share, Preview, Rename, Download, Delete, Properties.
  - **Folders**: Share (pending answer), Rename, Download, Delete, Folder statistics.
- Clicking an action will trigger callbacks to the ViewModel.

#### [MODIFY] `net.m21xx.s3explorer.ui.explorer.FileExplorerItems.kt`
- Add an `IconButton` with `Icons.Default.MoreVert` to the right side of `CompactListItem`, `DetailedListItem`, `FolderItem`, `GalleryCardItem`, and `GalleryFolderCardItem`.
- Trigger the `ObjectActionBottomSheet` state when clicked.

#### [MODIFY] `net.m21xx.s3explorer.ui.explorer.FileExplorerScreen.kt`
- Add state to track which object is currently selected for the context menu (e.g., `var contextObject by remember { mutableStateOf<S3ObjectEntity?>(null) }`).
- Include the `ObjectActionBottomSheet` in the view hierarchy when `contextObject != null`.
- Add UI state for the Rename, Delete confirmation, and Properties modals.

#### [MODIFY] `net.m21xx.s3explorer.ui.viewer.MediaViewerScreen.kt`
- Add a three-dots menu to the top-right corner of the TopAppBar.
- Reuse the exact same `ObjectActionBottomSheet` component.

### 2. Transfers Drawer & State Management

#### [NEW] `net.m21xx.s3explorer.domain.TransferManager.kt`
- Create a Singleton class responsible for holding active transfers.
- Define a `TransferState` data class (Filename, TotalBytes, TransferredBytes, Status: Progress/Success/Error).
- Expose a `StateFlow<List<TransferState>>` for the UI to observe.
- Provide functions to `pause()`, `resume()`, and `cancel()` specific transfers.

#### [MODIFY] `net.m21xx.s3explorer.domain.UploadObjectUseCase.kt` & `DownloadObjectUseCase.kt` (New)
- Refactor the AWS SDK calls (e.g., `ByteStream`) to intercept reads/writes and emit progress updates to the `TransferManager`.

#### [NEW] `net.m21xx.s3explorer.ui.explorer.components.TransfersBottomSheet.kt`
- Create the interactive Transfers Drawer as a `ModalBottomSheet`.
- **Header**: Display aggregate progress (e.g., "5.00 MB of 10.00 MB | (50%)") and calculate dynamic speed (MB/s).
- **List Items**: Display borderless rows for each transfer containing:
  - Left Block: Filename (word wrapped), horizontal progress bar, and size stats.
  - Right Block: Pause/Resume Icon, Cancel Icon.
- Automatically dismiss the drawer if the list of active transfers becomes empty.

#### [MODIFY] `net.m21xx.s3explorer.ui.navigation.S3Navigation.kt`
- Based on the answer to Open Question #3, potentially remove the `Destinations.TRANSFERS` placeholder route.

## Verification Plan

### Automated Tests
- Build verification (`./gradlew assembleDebug`) to ensure all new UI components compile successfully.
- Ensure the `TransferManager` logic correctly parses sizes and calculates percentages without crashing.

### Manual Verification
- Tap the three-dots menu on a file and folder in the `FileExplorerScreen` and verify the `ObjectActionBottomSheet` slides up with the correct contextual options.
- Open the Media Viewer and verify the three-dots menu opens the same bottom sheet.
- Start a mock transfer (upload/download) and open the Transfers Drawer from the Connection Drawer to verify dynamic progress updates and proper formatting of units (B, KB, MB, GB).
