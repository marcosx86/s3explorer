# Region Configuration & Crash Prevention Walkthrough

This sub-phase resolves the S3 region mismatch crash (`expecting 'one'`) by allowing custom regions to be configured during connection setup, and wraps the sync background process in robust exception handling.

## What was completed

* **AWS Region Support**:
  * **Database & Entities**: Added `region: String` (defaulting to `"us-east-1"`) to `ConnectionProfileEntity`. Room DB has been bumped to version 3. Destructive migration will automatically recreate your tables.
  * **Domain Layer**: Updated `SaveConnectionProfileUseCase` and `FetchAvailableBucketsUseCase` to accept and pass through the custom region.
  * **Network DataSource**: Refactored `S3NetworkDataSource.listBuckets()` and `listObjects()` to use the configured region name in request signatures instead of hardcoding `us-east-1`.
  * **ViewModel & UI**: Added `region` state and a new **Region (Optional)** text field to `NewConnectionScreen.kt`. It defaults to `us-east-1` if left blank.

* **Crash Prevention (S3Exception Handling)**:
  * **ViewModel**: Wrapped the `SyncDirectoryUseCase.execute` call in a `try-catch` block inside `FileExplorerViewModel`. Instead of propagating raw exceptions and crashing the app, it captures the error string in `FileExplorerState.errorMessage`.
  * **UI**:
    * **Empty Directory Error**: Displays a clean, full-screen `ErrorState` layout with an explicit warning icon, the error message, and a **Retry** button.
    * **Incremental Error (Directory already loaded)**: Uses a `SnackbarHost` in the Scaffold and a `LaunchedEffect` to display transient errors as Snackbars with a "Retry" action, keeping the existing directory cached in view.

## Verification

> [!TIP]
> **Try Your Custom Setup!**
> Build the project in Android Studio. On the connection screen, enter `'one'` in the **Region** field to match your local setup.
> 
> You should also try typing a wrong region first to verify that the app no longer crashes! You'll see the full-screen retry layout. Once you retry with the correct region, it will load perfectly.

## Further Investigation Note

> [!NOTE]
> As requested, I have documented the requirement to investigate **automatic region detection** (e.g. discovering when a local MinIO engine is used and automatically applying the `"one"` region or extracting the expected region from standard AWS signature headers). We will tackle this in a future optimization phase!
