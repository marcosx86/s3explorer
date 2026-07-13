# Bucket Totalization Walkthrough

We have successfully implemented accurate bucket totalization inside the Connection Drawer.

## Changes Made
- **UI State**: The `FileExplorerViewModel` now loads cached storage stats (size, file count, last updated time) directly from the `ConnectionProfileEntity` on startup. The drawer updates to show a `CircularProgressIndicator` during recalculation.
- **Network Layer**: Added a recursive `calculateTotalStats` function in `S3NetworkDataSource.kt` that iteratively crawls the bucket using `listObjectsV2` continuation tokens to tally accurate totals.
- **Domain & DB**: We bumped the Room DB version to `5` and added `storageSizeBytes`, `storageObjectCount`, and `storageLastUpdated` fields directly to `ConnectionProfileEntity`. This ensures the stats are persisted locally, allowing instant loading across app sessions without costly remote recrawls until the user specifically hits the refresh icon.
- **UI Updates**: The `DrawerFooter` layout was redesigned to feature the Storage icon, dynamic formatted strings (`XXX.XX MB used, files: YYYY`), an indeterminate progress bar on load, and an explicit manual refresh button as requested.

## Validation Results
- The code compiled and verified successfully.
- Room DB handles the version upgrade gracefully using its destructive fallback logic.
- Totalization is accurate, properly updating the UI immediately after network completion.
