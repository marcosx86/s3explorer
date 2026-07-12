# Connection Drawer Implementation Plan

Implement the main navigation drawer for the File Explorer screen, fulfilling the architectural and functional requirements outlined in `docs/17-connection-drawer.md`. This drawer acts as the central command hub for both bucket-specific and global app settings.

## User Review Required

- **Storage Stats**: S3 does not natively support folder size querying without iterating all objects. For the initial implementation, `CalculateStorageStatsUseCase` will simulate a calculation or execute a naive `S3ObjectDao` query (summing cached objects). Full iterative bucket crawling will be implemented in a later pass if needed. Is this acceptable?
- **Destructive Actions**: The "Remove credentials" purge action will initially be scaffolded to show the confirmation dialog, but the deep wiping logic (`PurgeProfileUseCase`) will be fully implemented when the credential management epic is tackled. Is this acceptable?

## Proposed Changes

### Data & Domain Layer

#### [MODIFY] `app/src/main/java/net/m21xx/s3explorer/data/repository/ConnectionRepository.kt`
- Ensure `getProfileById(profileId: String)` is exposed from `ConnectionProfileDao` to the Repository.

#### [NEW] `app/src/main/java/net/m21xx/s3explorer/domain/CalculateStorageStatsUseCase.kt`
- Create a simple Use Case to query the `S3ObjectDao` for total object count and sum of `size` for a given `profileId` and `bucketName`.
- Output: `StorageStatsSummary(val sizeBytes: Long, val objectCount: Int, val lastUpdated: Long)`

#### [NEW] `app/src/main/java/net/m21xx/s3explorer/domain/ForceSyncUseCase.kt`
- Similar to `SyncDirectoryUseCase`, but bypasses the local cache and forces a fresh network fetch from S3 for the current prefix.

### UI State & ViewModels

#### [MODIFY] `app/src/main/java/net/m21xx/s3explorer/ui/explorer/FileExplorerState.kt`
- Add `DrawerUIState` sub-state:
  ```kotlin
  data class DrawerUIState(
      val activeProfile: ConnectionProfileEntity? = null,
      val storageStats: StorageStatsSummary? = null,
      val showAboutDialog: Boolean = false,
      val showRemoveCredentialsDialog: Boolean = false
  )
  ```
- Add `drawerState: DrawerUIState = DrawerUIState()` to `FileExplorerState`.

#### [MODIFY] `app/src/main/java/net/m21xx/s3explorer/ui/explorer/FileExplorerViewModel.kt`
- Inject `ConnectionRepository`, `CalculateStorageStatsUseCase`, and `ForceSyncUseCase`.
- On `init`, fetch the active `ConnectionProfileEntity` and update `drawerState.activeProfile`.
- Add methods: `refreshStorageStats()`, `forceSync()`, `toggleAboutDialog()`, `toggleRemoveCredentialsDialog()`.

### UI Components (Compose)

#### [NEW] `app/src/main/java/net/m21xx/s3explorer/ui/explorer/components/ConnectionDrawer.kt`
- Implement the `ModalDrawerSheet` containing:
  - **Header**: Profile Avatar, Alias, Endpoint IP/URL.
  - **Menu Group 1**: Account Settings, Transfers.
  - **Menu Group 2**: Sync, Settings, Media Backup, Trash, About, Remove Credentials.
  - **Footer**: Storage Stats widget (size, count, relative time).
- Implement the "About" `AlertDialog` with the placeholder text.

#### [MODIFY] `app/src/main/java/net/m21xx/s3explorer/ui/explorer/FileExplorerScreen.kt`
- Wrap the existing `Scaffold` inside a `ModalNavigationDrawer`.
- Bind `DrawerState` to control opening/closing.
- Update `onOpenDrawer` in `TopAppBar` to trigger the drawer state.

### Navigation Routing

#### [MODIFY] `app/src/main/java/net/m21xx/s3explorer/ui/navigation/S3Navigation.kt`
- Add placeholder destination strings in the `Destinations` object for: `ACCOUNT_SETTINGS`, `TRANSFERS`, `GLOBAL_SETTINGS`, `MEDIA_BACKUP`, `TRASH`.
- Add placeholder `composable` screens for these new destinations with simple "Coming Soon" or back buttons.
- Wire the `FileExplorerScreen` callbacks to navigate to these routes via the `NavController`.

## Verification Plan

### Automated Tests
- Run Gradle `compileDebugKotlin` to verify the new navigation routes, ViewModel injections, and Compose components compile successfully.

### Manual Verification
- Run the app, enter a bucket explorer.
- Tap the hamburger menu; verify the drawer slides out.
- Verify the header displays the correct profile alias and endpoint.
- Verify tapping "About" opens the placeholder dialog.
- Verify tapping other items navigates to the placeholder screens and closes the drawer.
