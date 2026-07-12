# S3 Explorer UI: Phase 1 Implementation Plan

This plan outlines the first major step in implementing the UI, focusing on the foundational theming, navigation, and the critical "New Connection" screen.

## User Review Required

> [!IMPORTANT]
> **Aesthetics & Theming:** I plan to use a sleek, premium dark mode by default, leveraging modern typography (like Inter or Roboto) and subtle micro-animations for focus states. If you have specific brand colors in mind, please let me know. 

## Open Questions

- Should the `NewConnectionViewModel` mock its network responses for now so we can test the UI transitions and error states without actually hitting a real S3 bucket?

## Proposed Changes

### 1. Theming Foundation (`ui/theme/`)
- Establish a rich, modern typography and color scheme (Material 3). We will prioritize a premium aesthetic.
- **[MODIFY]** `Theme.kt`, `Color.kt`, and `Type.kt` to define the baseline design system.

### 2. Navigation Scaffolding (`ui/navigation/`)
- **[NEW]** `S3Navigation.kt`: Define a `NavHost` using Jetpack Navigation Compose.
- Define route objects for type-safe navigation (e.g., `NewConnectionRoute`, `FileExplorerRoute`).
- **[MODIFY]** `MainActivity.kt`: Update the entry point to load the NavHost instead of the dummy text.

### 3. New Connection Screen (`ui/connection/`)
- **[NEW]** `NewConnectionScreen.kt`: Compose layout matching the requirements from `02-new-connection.md`.
  - Input fields: Access Key, Secret Key (with visibility toggle), Endpoint URL, Bucket Name.
  - Interactive elements: 'Connect' button, Terms of Service checkbox.
  - Styling: Material 3 Outlined text fields with clear error state indicators.
- **[NEW]** `NewConnectionViewModel.kt`: State holder managing form validation (checking for empty fields) and UI state (`idle`, `testing`, `success`, `error`).
- **[NEW]** `NewConnectionState.kt`: Data class wrapping the form fields and their validation status.

## Verification Plan

### Automated Tests
- Add a Compose UI test (`NewConnectionScreenTest.kt`) to verify that the "Connect" button is initially disabled and becomes enabled only when all required fields (URL, Keys, TOS) are filled.

### Manual Verification
- Deploy the app to the emulator.
- Verify the new dark theme is applied.
- Test the form validation logic interactively (toggling the password visibility, checking the TOS, and triggering a mock connection test).
