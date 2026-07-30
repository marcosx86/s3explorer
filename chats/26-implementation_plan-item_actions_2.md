# Object Context Actions Implementation Plan

This plan details the implementation of the stubbed contextual actions within the `ObjectActionBottomSheet` (Rename, Delete, Folder Stats, and Object Properties).

## User Review Required

> [!WARNING]
> **Rename Implementation**: In AWS S3, there is no direct "rename" command. Renaming an object requires copying the object to the new key and then deleting the old object. This process will trigger network traffic proportional to the object size internally within AWS, but shouldn't affect the client's bandwidth. Are you okay with implementing rename as a standard Copy + Delete?

> [!IMPORTANT]
> **Folder Deletions**: Deleting a folder in S3 actually involves listing and deleting all objects matching that prefix. This can be destructive and slow for very large folders. We will add a confirmation dialog warning the user before proceeding.

## Proposed Changes

### Network Layer
#### [MODIFY] [S3NetworkDataSource.kt](file:///c:/git/s3explorer/app/src/main/java/net/m21xx/s3explorer/data/remote/S3NetworkDataSource.kt)
- Add `deleteObject` using the S3 client's `DeleteObjectRequest`.
- Add `deleteObjects` using `DeleteObjectsRequest` for batch deletions (useful for folders).
- Add `copyObject` using `CopyObjectRequest` to duplicate an object (first step of Rename).
- Add `getObject` to retrieve an object's contents for download.

### Domain Layer (Use Cases)
#### [NEW] [DeleteObjectUseCase.kt](file:///c:/git/s3explorer/app/src/main/java/net/m21xx/s3explorer/domain/DeleteObjectUseCase.kt)
- Given a profile ID, bucket name, and object key (or prefix), issue a delete request. If it's a folder (ends with `/`), list all objects under that prefix and batch delete them.

#### [NEW] [RenameObjectUseCase.kt](file:///c:/git/s3explorer/app/src/main/java/net/m21xx/s3explorer/domain/RenameObjectUseCase.kt)
- Coordinates `copyObject` to the new key, followed by `deleteObject` on the old key. Fails gracefully if copy fails.

#### [NEW] [DownloadObjectUseCase.kt](file:///c:/git/s3explorer/app/src/main/java/net/m21xx/s3explorer/domain/DownloadObjectUseCase.kt)
- Hooks into `TransferManager` to download the object to the local device's `Downloads` directory, decrypting it if E2E encryption is enabled.

#### [NEW] [GetFolderStatsUseCase.kt](file:///c:/git/s3explorer/app/src/main/java/net/m21xx/s3explorer/domain/GetFolderStatsUseCase.kt)
- Traverses a specific prefix (folder) and tallies up the total size and file count.

### Presentation Layer
#### [MODIFY] [FileExplorerViewModel.kt](file:///c:/git/s3explorer/app/src/main/java/net/m21xx/s3explorer/ui/explorer/FileExplorerViewModel.kt)
- Inject the new use cases.
- Add UI state properties for tracking which dialog is currently open:
  - `objectToDelete`
  - `objectToRename`
  - `objectToShowProperties`
- Add action methods (`confirmDelete`, `confirmRename`, `initiateDownload`, `loadProperties`).

#### [NEW] [ObjectActionDialogs.kt](file:///c:/git/s3explorer/app/src/main/java/net/m21xx/s3explorer/ui/explorer/components/ObjectActionDialogs.kt)
- Extracted composables for the confirmation dialogs to keep `FileExplorerScreen.kt` clean:
  - `DeleteConfirmationDialog`
  - `RenameDialog`
  - `PropertiesDialog` (Shows sizes, E2E encryption status, raw metadata).

#### [MODIFY] [FileExplorerScreen.kt](file:///c:/git/s3explorer/app/src/main/java/net/m21xx/s3explorer/ui/explorer/FileExplorerScreen.kt)
- Integrate `ObjectActionDialogs`. Wire the click handlers in `ObjectActionBottomSheet` to trigger these dialogs via the ViewModel's exposed state.

## Verification Plan

### Automated Tests
- Unit tests for the new UseCases (especially Rename to ensure delete is only called if copy succeeds).

### Manual Verification
- Deploy to device/emulator.
- Create a test file, rename it, and verify it updates in the UI.
- Delete the file and verify it's removed.
- Open properties on a file and verify metadata is populated.
