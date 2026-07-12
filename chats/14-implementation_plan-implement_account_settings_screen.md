# Account Settings Implementation Plan

Implement the Account Settings screen and its underlying data architecture, as described in `docs/12-settings-and-global-options.md`. This screen manages settings isolated by the active `profileId`, ensuring secure and customized behaviors per S3 connection.

## User Review Required

- **DataStore Isolation:** The plan uses `PreferenceDataStoreFactory` to dynamically create DataStores named after the `profileId` (e.g., `profile_1234.preferences_pb`). This effectively isolates settings. Is this the intended architectural approach for the dynamic instantiation?
- **Cache Clearing:** The actions "Clear document cache" and "Clear thumbnail cache" will currently just be scaffolded out in UseCases (e.g. `ClearCacheUseCase`), emitting a success message to the UI. The actual deep file deletion logic can be hooked up once the caching systems are finalized. Is this acceptable?

## Proposed Changes

### Data & Domain Layer

#### [NEW] `app/src/main/java/net/m21xx/s3explorer/data/local/preferences/ProfilePreferencesDataStore.kt`
- Create a factory/manager class injected with `@ApplicationContext`.
- Dynamically instantiate and cache `DataStore<Preferences>` instances based on `profileId`.
- Expose `Flow`s and `suspend` mutator functions for:
  - `filenameEncryptionEnabled` (Boolean, default: false)
  - `multipartUploadThresholdMB` (Int, default: 5)
  - `uploadConcurrency` (Int, default: 3)
  - `calculateMD5Enabled` (Boolean, default: false)

#### [NEW] `app/src/main/java/net/m21xx/s3explorer/domain/ClearCacheUseCase.kt`
- Scaffold a use case that takes `profileId` and `cacheType` (e.g., `DOCUMENTS`, `THUMBNAILS`) and returns a `Result<Unit>`.

### ViewModels

#### [NEW] `app/src/main/java/net/m21xx/s3explorer/ui/settings/AccountSettingsViewModel.kt`
- Create a new ViewModel taking `SavedStateHandle` to retrieve the `profileId` argument.
- Inject `ProfilePreferencesDataStore` and `ClearCacheUseCase`.
- Expose a `StateFlow<AccountSettingsUIState>` combining the DataStore preferences.
- Provide functions to update each setting and trigger the clear cache actions.

### UI Components (Compose)

#### [NEW] `app/src/main/java/net/m21xx/s3explorer/ui/settings/AccountSettingsScreen.kt`
- Implement the settings UI containing:
  - TopAppBar with a Back button and title "Account Settings".
  - **Cryptography & Privacy Section:**
    - Switch for "Filename encryption".
  - **Network & Transfer Tuning Section:**
    - Slider (or text input) for "Start threshold (MB)".
    - Slider for "Upload transfers" concurrency.
    - Switch for "Calculate MD5 hash".
  - **Cache Lifecycle Management Section:**
    - Action rows/buttons for "Clear document cache" and "Clear thumbnail cache", triggering a confirmation Snackbar.
- Replace the `PlaceholderScreen` for `Destinations.ACCOUNT_SETTINGS` in `S3Navigation.kt` with this new component.

## Verification Plan

### Automated Tests
- Run Gradle `compileDebugKotlin` to verify the new DataStore manager, ViewModel, and Compose screen compile successfully.

### Manual Verification
- Tap "Account settings" in the navigation drawer.
- Verify navigation transitions to the Account Settings screen.
- Verify changing a setting (like a toggle) is persisted locally by backing out of the screen and re-entering to see the state restored.
