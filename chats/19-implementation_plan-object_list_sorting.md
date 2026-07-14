# Item Sorting & Hidden Files Support

This plan details how we'll implement fully dynamic sorting (Name, Size, Type, Last Modified) and hidden file filtering right from the File Explorer toolbar, persisted directly to user preferences.

## User Review Required

> [!NOTE]
> Adding a new `extension` field to our local Room Database requires bumping the database version (from 5 to 6). Since we have `fallbackToDestructiveMigration()` enabled, this will seamlessly drop the current cached tables. Users will just see a quick re-sync spinner the next time they open a bucket. No remote data is affected.

## Open Questions
None. The UI request is very specific and fully covers standard sorting operations.

## Proposed Changes

### Data & Domain Layer
- **[MODIFY] [AppDatabase.kt](file:///c:/git/s3explorer/app/src/main/java/net/m21xx/s3explorer/data/local/AppDatabase.kt)**
  - Bump `version` from 5 to 6.
- **[MODIFY] [S3ObjectEntity.kt](file:///c:/git/s3explorer/app/src/main/java/net/m21xx/s3explorer/data/local/entity/S3ObjectEntity.kt)**
  - Add `val extension: String` so we can efficiently perform SQL `ORDER BY extension` for "Sort by Type".
- **[MODIFY] [SyncDirectoryUseCase.kt](file:///c:/git/s3explorer/app/src/main/java/net/m21xx/s3explorer/domain/SyncDirectoryUseCase.kt)**
  - Update mapping logic to compute and save the file `extension` (folders will have an empty extension).
- **[MODIFY] [S3ObjectDao.kt](file:///c:/git/s3explorer/app/src/main/java/net/m21xx/s3explorer/data/local/dao/S3ObjectDao.kt)**
  - Add `@RawQuery(observedEntities = [S3ObjectEntity::class])` to allow dynamic sorting via `SupportSQLiteQuery`.

### State & Preferences
- **[NEW] ExplorerEnums.kt** (or similar file in `ui/explorer/`)
  - Define `enum class SortBy { NAME, SIZE, TYPE, LAST_MODIFIED }`
  - Define `enum class SortDirection { ASCENDING, DESCENDING }`
- **[MODIFY] [SettingsDataStore.kt](file:///c:/git/s3explorer/app/src/main/java/net/m21xx/s3explorer/data/local/preferences/SettingsDataStore.kt)**
  - Add persistent keys for `sortBy` and `sortDirection`. (We will reuse the existing `hideDotfiles` preference for the hidden files toggle).
- **[MODIFY] [FileExplorerState.kt](file:///c:/git/s3explorer/app/src/main/java/net/m21xx/s3explorer/ui/explorer/FileExplorerState.kt)**
  - Add `sortBy`, `sortDirection`, and `showHidden` (inverse of `hideDotfiles`) to the UI state.
- **[MODIFY] [FileExplorerViewModel.kt](file:///c:/git/s3explorer/app/src/main/java/net/m21xx/s3explorer/ui/explorer/FileExplorerViewModel.kt)**
  - Observe the new preferences and pipe them into `ObserveDirectoryContentUseCase`.
  - Add functions to update sorting and toggle hidden files.

### Use Case
- **[MODIFY] [ObserveDirectoryContentUseCase.kt](file:///c:/git/s3explorer/app/src/main/java/net/m21xx/s3explorer/domain/ObserveDirectoryContentUseCase.kt)**
  - Accept `sortBy`, `sortDirection`, and `showHidden`.
  - Construct a dynamic `SimpleSQLiteQuery`:
    - Applies `ORDER BY isDirectory DESC, [SortField] [Direction]`
    - Conditionally adds `AND objectKey NOT LIKE :parentPrefix || '.%'` if `showHidden` is false.

### UI Layer
- **[MODIFY] [FileExplorerScreen.kt](file:///c:/git/s3explorer/app/src/main/java/net/m21xx/s3explorer/ui/explorer/FileExplorerScreen.kt)**
  - Add a Sort Icon (`Icons.Default.Sort`) before the View Mode toggle.
  - Implement a `DropdownMenu` with sections:
    - **Sort by**: Radio-style selection for Name, Size, Type, Last updated.
    - **Direction**: Radio-style selection for Ascending, Descending.
    - **Toggle**: Checkbox style for "Show hidden".

## Verification Plan

### Automated Tests
- Run `./gradlew compileDebugKotlin` to ensure no syntax or integration issues.

### Manual Verification
- Deploy to device/emulator.
- Open a bucket containing multiple file types and dotfiles (`.env`, `.gitignore`).
- Toggle "Show hidden" and verify dotfiles disappear/reappear.
- Toggle sorting options and verify ordering updates instantly across pagination.
