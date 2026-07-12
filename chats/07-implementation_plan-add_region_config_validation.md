# S3 Explorer: Region Configuration & Crash Prevention Plan

This plan addresses the S3 region validation crash (`expecting 'one'`) by making the AWS region configurable in the connection profile and adding proper exception handling in the explorer UI to prevent crashes.

## User Review Required

> [!IMPORTANT]
> **Database Recreation:** Adding the `region` field to our Room entities will bump the database version to `3`. Because we enabled `.fallbackToDestructiveMigration()`, this will automatically wipe and recreate the local database when you run the app. Your previously saved connection profiles will be cleared. Is this acceptable?
> 
> **UI Layout:** I propose adding the "Region" field as an optional field in the Connection Screen, defaulting to `us-east-1` if left blank.

## Proposed Changes

### 1. Robust Exception Handling (Explorer UI)
- **[MODIFY]** `FileExplorerState.kt`: Add `errorMessage: String? = null`.
- **[MODIFY]** `FileExplorerViewModel.kt`: Wrap the `syncDirectoryUseCase.execute` call in a `try-catch` block. Catch any `Exception`, update the state's `errorMessage`, and clear it on a successful sync.
- **[MODIFY]** `FileExplorerScreen.kt`: Display a `Snackbar` or inline error banner if `errorMessage` is present, allowing the user to retry.

### 2. Configurable AWS Region (Data & Domain)
- **[MODIFY]** `ConnectionProfileEntity.kt`: Add `val region: String` (defaults to `"us-east-1"`).
- **[MODIFY]** `SaveConnectionProfileUseCase.kt`: Accept `region` parameter and pass it to the profile entity.
- **[MODIFY]** `NewConnectionState.kt` & `NewConnectionViewModel.kt`: Add `region` state and wire it up to the test/save actions.
- **[MODIFY]** `S3NetworkDataSource.kt`: Use the provided `region` in both `listBuckets` and `listObjects` instead of hardcoding `"us-east-1"`.

### 3. Connection UI Update
- **[MODIFY]** `NewConnectionScreen.kt`: Add an "AWS Region" text field. It will pre-populate/default to `"us-east-1"`.

## Verification Plan

### Manual Verification
- Build and run the app.
- On the Connection Screen, type `one` in the Region field (or leave it as `us-east-1` to test the error handling).
- If you enter the wrong region, the app should NOT crash; instead, it should show a user-friendly error message on the Explorer screen.
- If you enter `one`, it should successfully connect and list the files.
