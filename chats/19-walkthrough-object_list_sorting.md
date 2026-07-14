# Splash Screen & Theming Walkthrough

The visual overhaul and splash screen integration are now complete. Here is a summary of the changes:

## Changes Made
- **Asset Extraction:** Extracted the high-resolution logo and background from the provided manual. Removed backgrounds using `rembg` (or manually by user fallback).
- **Splash Screen:** Implemented Android 12+ native Splash Screen API (`androidx.core:core-splashscreen`). It features the `splashscreen.png` animated icon against the `#2470A2` sky blue background, configured in `splash_theme.xml`.
- **Manifest Updates:** Configured the `android:icon` and `android:roundIcon` to use the provided `app_icon.png`, completing the branding.
- **Premium Material 3 Theming:** Overhauled the `Color.kt` and `Theme.kt` with a premium palette derived from the logo (Ocean Blue, Sky Blue, and Warm Wood).
- **Watermark Background:** Created a reusable `WatermarkBackground` component that renders a large, faint, grayscale, and desaturated logo in the bottom right corner of empty states (like an empty directory in the file explorer).

## 3. Explorer File Sorting
- **Data Persistence:** Added `sortBy`, `sortDirection`, and `showHidden` keys to `SettingsDataStore`.
- **Database Enhancement:** Bumped Room database to version 6 and added an `extension` column to `S3ObjectEntity`. Updated `SyncDirectoryUseCase` to automatically parse and store the extension (or empty string if none).
- **Dynamic Queries:** Replaced the static Room `@Query` with a dynamic `@RawQuery` using `SupportSQLiteQuery` in `S3ObjectDao`. The query is generated cleanly in `ObserveDirectoryContentUseCase` based on the active Sort state (Name, Size, Type, Last updated, Direction, and dotfile visibility).
- **UI Interaction:** A sort icon (`Icons.Default.Sort`) was added to the `FileExplorerScreen` TopAppBar. Clicking it opens a `DropdownMenu` with categorized selectable options that apply instantly and are remembered across sessions.

## Validation Results
- Database migrations execute cleanly.
- Splash screen displays the correct icon and gradients matching the provided logo.
- `ObserveDirectoryContentUseCase` seamlessly streams SQLite paging results while observing preference state.
- Empty folders show the transparent watermark icon as requested.
- Sorting applies immediately via Room raw queries and saves state seamlessly to DataStore.
- KSP errors caused by modern Kotlin versions have been successfully resolved by safely forcing KSP1 execution (`ksp.useKSP2=false`) and updating dependencies, bringing the build to a 100% success state.
- The UI handles the `Theme.App.Starting` seamlessly during the initial activity launch, transitioning smoothly to the newly themed `S3ExplorerTheme`.
- Visual components gracefully fall back or adapt based on the system's Light/Dark mode while preserving the custom color palette.

> [!TIP]
> The transparent watermark uses `ColorMatrix()` to convert the logo to grayscale programmatically, reducing the need for duplicate asset variants!
