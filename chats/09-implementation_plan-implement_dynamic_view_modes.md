# Dynamic View Modes Implementation Plan

This plan aims to build a robust, highly performant dynamic view switching system. I want to exceed your expectations by implementing a solution that completely bypasses the scroll-state preservation issues common when switching between `LazyColumn` and grids, and sets up a robust foundation for memory-efficient image loading with Coil.

## Proposed Architecture

Instead of swapping between a `LazyColumn` and a `LazyVerticalGrid` (which makes sharing scroll state impossible since they use `LazyListState` and `LazyGridState` respectively), **we will use a single `LazyVerticalGrid` for everything**.

By dynamically altering the `columns` property based on the `ExplorerViewMode`, we get 100% perfect scroll state preservation out of the box with a single hoisted `LazyGridState`.
The order of view modes and their grid setups will be:
- **DETAILED_LIST**: `GridCells.Fixed(1)` (Standard vertical list, full details)
- **COMPACT_LIST**: `GridCells.Fixed(1)` (Dense vertical list, no subtitles)
- **GALLERY_SMALL**: `GridCells.Fixed(2)` (2 image cards per line)
- **GALLERY_LARGE**: `GridCells.Fixed(1)` (1 large full-width image card per line)

---

### 1. Data Layer: Settings DataStore

We will implement the Android Jetpack `Preferences DataStore` to ensure the view mode choice persists across app restarts.

#### [NEW] [ExplorerViewMode.kt](file:///C:/git/s3explorer/app/src/main/java/net/m21xx/s3explorer/ui/explorer/ExplorerViewMode.kt)
- Enum class defining the four modes.
- Provide a `fun next(): ExplorerViewMode` helper to cycle through the states.
- Map each enum value to its corresponding icon:
  - `DETAILED_LIST` -> `Icons.Default.ViewAgenda`
  - `COMPACT_LIST` -> `Icons.Default.List`
  - `GALLERY_SMALL` -> `Icons.Default.ViewCozy`
  - `GALLERY_LARGE` -> `Icons.Default.CalendarViewDay`

#### [NEW] [SettingsDataStore.kt](file:///C:/git/s3explorer/app/src/main/java/net/m21xx/s3explorer/data/local/preferences/SettingsDataStore.kt)
- A class wrapping `androidx.datastore.preferences.core.Preferences`.
- Exposes a `Flow<ExplorerViewMode>` defaulting to `DETAILED_LIST`.
- Provides a `suspend fun setViewMode(mode: ExplorerViewMode)` to update it.

#### [MODIFY] [AppModule.kt](file:///C:/git/s3explorer/app/src/main/java/net/m21xx/s3explorer/di/AppModule.kt)
- Provide the `DataStore<Preferences>` instance as a Singleton to be injected into view models.

---

### 2. Domain & Presentation Layer

#### [MODIFY] [FileExplorerViewModel.kt](file:///C:/git/s3explorer/app/src/main/java/net/m21xx/s3explorer/ui/explorer/FileExplorerViewModel.kt)
- Inject `SettingsDataStore` and observe the `viewMode` flow.
- Add `fun toggleViewMode()` to allow the UI to cycle to the next state.

#### [MODIFY] [FileExplorerScreen.kt](file:///C:/git/s3explorer/app/src/main/java/net/m21xx/s3explorer/ui/explorer/FileExplorerScreen.kt)
- Add a new "View Mode" icon in the `TopAppBar` that dynamically displays the *current* view mode's icon and cycles when clicked.
- Replace the `LazyColumn` with a `LazyVerticalGrid`.
- Hoist `val gridState = rememberLazyGridState()` to preserve scroll position perfectly across view mode toggles.

---

### 3. UI Layer: Dynamic Composables & Coil

We will implement three distinct composables for rendering the `S3ObjectEntity`. Even though we aren't fetching real images from S3 yet, we will set up the Coil `AsyncImage` architecture exactly as required so it's plug-and-play later.

#### [MODIFY] [FileExplorerItems.kt](file:///C:/git/s3explorer/app/src/main/java/net/m21xx/s3explorer/ui/explorer/FileExplorerItems.kt)
- **`DetailedListItem`**: The current layout. 48dp placeholder, filename, size, date, overflow menu.
- **`CompactListItem`**: Dense row. Tiny 36dp placeholder, filename, overflow menu. No subtitles.
- **`GalleryCardItem`**: A beautiful Material 3 `ElevatedCard`. Square aspect ratio, large placeholder, filename overlay at the bottom. Used for both `GALLERY_SMALL` (it will shrink to half-width) and `GALLERY_LARGE` (it will stretch full-width).

#### Coil Integration Strategy (Future-Proofing)
In each composable, we will use `AsyncImage` with an explicit `ImageRequest.Builder(LocalContext.current)`.
- For Compact/Detailed: `.size(128, 128).crossfade(true)`
- For Gallery: `.size(1080, 1080).crossfade(true)`
- *Note:* Since we don't have a secure Coil fetcher for S3 set up yet, the `model` will be set to fallback to our standard `Icons.Default.InsertDriveFile` and `Icons.Default.Image`, but the RAM-saving architecture will be locked in.

## Verification Plan
1. Switch to the File Explorer screen.
2. Tap the new View Mode toggle icon in the top app bar.
3. Observe the layout instantly cycle between: Detailed List -> Compact List -> Gallery Small -> Gallery Large.
4. Scroll halfway down a large directory, toggle the view mode, and verify the scroll position remains perfectly locked on the same files.
5. Restart the app and verify the last selected view mode persists.
