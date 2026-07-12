# Global Settings Implementation Plan

Implement the Global Settings screen as described in `docs/12-settings-and-global-options.md`. This screen manages preferences that dictate app-wide behavior across the entire device, regardless of the active S3 connection.

## User Review Required

- **Action Implementations**: Toggles like "Enable native lock screen" or "Enable Mount (SAF)" require complex Android OS-level integrations (Biometric prompt hooks and `DocumentsProvider` implementations). This initial epic will focus purely on building out the UI screen and wiring it to the local DataStore. The actual system integrations will be left as future tasks. Is this acceptable?
- **SSL Certificates**: The "Trust SSL certificate (PEM)" option will currently be implemented as a simple boolean toggle representing whether custom certificates are allowed, rather than a full PEM file picker for this MVP layout. Is this acceptable?

## Proposed Changes

### Data & Domain Layer

#### [MODIFY] `app/src/main/java/net/m21xx/s3explorer/data/local/preferences/SettingsDataStore.kt`
- Introduce new `booleanPreferencesKey` keys for:
  - `enable_saf_mount`
  - `trust_ssl_certificate`
  - `enable_lock_screen`
  - `display_long_date`
  - `hide_dotfiles`
  - `show_thumbnails` (Default to true)
- Group these settings into a new data class `GlobalPreferences`.
- Expose a unified `Flow<GlobalPreferences>` alongside the existing `viewMode` flow.
- Add corresponding `suspend` setter functions for each property.

### ViewModels

#### [NEW] `app/src/main/java/net/m21xx/s3explorer/ui/settings/GlobalSettingsViewModel.kt`
- Create an `@HiltViewModel` injecting `SettingsDataStore`.
- Expose `uiState: StateFlow<GlobalPreferences>`.
- Provide functions to toggle each of the boolean preferences.

### UI Components (Compose)

#### [NEW] `app/src/main/java/net/m21xx/s3explorer/ui/settings/GlobalSettingsScreen.kt`
- Implement a Material 3 settings interface containing:
  - TopAppBar with a Back button and title "Global Settings".
  - **System Integration & OS Mounting Section:**
    - Switch for "Enable Mount (SAF)".
    - Switch for "Trust SSL certificate (PEM)".
  - **App Security Section:**
    - Switch for "Enable native lock screen (Biometrics)".
  - **View Preferences Section:**
    - Switch for "Display long date format".
    - Switch for "Hide dotfiles".
    - Switch for "Show image thumbnails".
- Replace the `PlaceholderScreen` for `Destinations.GLOBAL_SETTINGS` in `S3Navigation.kt` with this component.

## Verification Plan

### Automated Tests
- Run Gradle `compileDebugKotlin` to ensure the extended DataStore logic, the new ViewModel, and the new Screen compile without issues.

### Manual Verification
- Navigate to "Settings" from the Connection Drawer.
- Verify the toggles render correctly and can be changed.
- Verify that leaving the screen and returning retains the toggled state, confirming data persistence to the Global DataStore.
