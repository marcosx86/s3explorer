# Goal Description

Implement the enhancements defined in `specs/21-bucket-explorer-features.md` to upgrade the bucket file explorer interface, including a floating action drawer, selection mode, search/filtering, and a "folders first" sort toggle.

## Open Questions
- **Upload File / Take Photo**: Do you want the `ActivityResultContracts` (file picker and camera) to be fully implemented in this iteration, or should I stub the callbacks with empty functions for now?
- **Selection Mode Operations**: Should any bulk actions (e.g., bulk delete, bulk download) appear in the toolbar when items are selected, or are we just implementing the UI selection mode for now?

## Proposed Changes

### 1. State and ViewModel Layer
#### [MODIFY] `net.m21xx.s3explorer.ui.explorer.FileExplorerState`
Add the following state properties:
- `showFloatingActionDrawer: Boolean = false`
- `selectionModeActive: Boolean = false`
- `selectedItems: Set<String> = emptySet()`
- `searchActive: Boolean = false`
- `searchQuery: String = ""`
- `foldersFirst: Boolean = true`

#### [MODIFY] `net.m21xx.s3explorer.ui.explorer.FileExplorerViewModel`
Add state mutators:
- `toggleFloatingActionDrawer(show: Boolean)`
- `toggleSelectionMode()`
- `toggleItemSelection(objectKey: String)`
- `toggleSearch()`
- `updateSearchQuery(query: String)`
- `toggleFoldersFirst()`
Update the `pagedObjects` flow to pass the new `searchQuery` and `foldersFirst` parameters to `observeDirectoryContentUseCase`.

### 2. Domain and Data Layer
#### [MODIFY] `net.m21xx.s3explorer.domain.ObserveDirectoryContentUseCase`
Update the `execute()` signature to accept `searchQuery: String` and `foldersFirst: Boolean`.
Update the SQLite query builder:
- **Search**: If `searchQuery` is not blank, append `AND objectKey LIKE ?` and pass `%searchQuery%` to the query arguments.
- **Sorting**: Conditionally include `isDirectory DESC,` in the `ORDER BY` clause based on the `foldersFirst` boolean flag.

### 3. UI Presentation Layer
#### [MODIFY] `net.m21xx.s3explorer.ui.explorer.FileExplorerScreen`
- **Floating Action Drawer**: Implement a `ModalBottomSheet` triggered when `showFloatingActionDrawer == true`. Add rows for `New folder`, `Upload file`, and `Take photo`. Hook the existing "Add" (Plus) button to open this drawer.
- **Search Bar**: Modify the TopAppBar to show an `OutlinedTextField` for searching when `searchActive` is true. Hook the Search icon button to toggle this state.
- **Sorting Menu**: Add a "Folders first" switch inside the sorting dropdown right below the "Show hidden" option.
- **Selection Mode Toolbar**: Update the TopAppBar icons or background color slightly when `selectionModeActive` is true to indicate bulk selection state. Hook the Checklist icon to toggle selection mode.

#### [MODIFY] `net.m21xx.s3explorer.ui.explorer.FileExplorerItems`
Update all list and gallery item composables (`FolderItem`, `DetailedListItem`, `CompactListItem`, `GalleryCardItem`, `GalleryFolderCardItem`):
- Pass `selectionModeActive: Boolean`, `isSelected: Boolean`, and `onSelect: () -> Unit` parameters.
- Conditionally render a `Checkbox` (e.g., wrapped in an `AnimatedVisibility`) on the left side of the items or overlaid on the gallery cards when `selectionModeActive` is true.

## Verification Plan

### Automated Tests
- The Gradle build will verify there are no compilation or typing issues introduced across the layers.

### Manual Verification
- **FAB Drawer**: Verify tapping the Plus icon slides up a bottom sheet with the correct options.
- **Search**: Verify typing in the search bar dynamically filters the list of files/folders in the current directory.
- **Sorting**: Verify toggling "Folders first" mixes folders alphabetically with files when disabled, and pins them to the top when enabled.
- **Selection Mode**: Verify clicking the checklist icon exposes checkboxes on every list and gallery item, and that checking them updates the state.
