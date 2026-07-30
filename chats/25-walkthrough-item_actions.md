# Linking Upload Settings Walkthrough

We've successfully integrated the user-defined upload settings into our `UploadObjectUseCase` and underlying layers. Here is what has been implemented:

## Changes Made

### 1. Security & Profile Preferences
- **E2E Encryption**: Added `e2eEncryptionEnabled` to `ProfilePreferences` and the `ProfilePreferencesDataStore`.
- **Passphrase Storage**: Added methods in `SecureStorage` and `ConnectionRepository` to securely store and retrieve the E2E passphrase alongside the S3 secret key via Android's `EncryptedSharedPreferences`. 

### 2. S3 Network Modifications
- Modified `S3NetworkDataSource.uploadObject` to accept optional parameters: `storageClass`, `contentMd5`, and `metadata`.
- Added a new method `S3NetworkDataSource.headObject` to retrieve remote S3 object metadata. This lets us peek at an existing object's size and custom properties before uploading.

### 3. S3 Upload Use Case Enhancements
`UploadObjectUseCase` now checks the current connection profile's preferences right before uploading. 

- **Skip Same File Upload**: 
  - For files >= 10KB, the app now uses `headObject` to verify if the object already exists on S3.
  - If the remote size matches the local size, and if both the local and remote modification times (`x-amz-meta-mtime`) match, the upload is silently skipped. If neither time is provided but the size matches exactly, it is also skipped.
- **E2E Encryption**: 
  - If enabled, the file bytes are encrypted locally via `AES/GCM/NoPadding` using the secure passphrase.
  - The unique Initialization Vector (IV) is stored as custom metadata on S3 (`x-amz-meta-iv`) so it can be reliably decrypted during download later.
- **MD5 Calculation**: 
  - If enabled, the MD5 digest of the payload is calculated (after encryption, if applicable) and sent via the `Content-MD5` header.
- **Storage Class**: 
  - If configured, the `storageClass` parameter is passed down to the S3 put request.

### 4. UI Layer Fixes
- **File Explorer Screen**: The `filePickerLauncher` and `cameraLauncher` were updated to actively extract the `lastModified` time of the local file and safely pass it down into the `UploadObjectUseCase`.

## What was Tested
- Type checking across layers.
- Validated signature mapping in `FileExplorerViewModel` and `FileExplorerScreen`.

> [!NOTE]  
> The E2E Encryption uses AES-GCM and stores the Initialization Vector directly in the S3 metadata as `x-amz-meta-iv`. This is standard practice in cryptographic applications such as the official AWS Encryption Client, as the IV is public and only required to be unique per payload.

---

# Object Context Actions and Transfers Drawer Walkthrough

We have successfully implemented the infrastructure and UI components for contextual actions and the active transfers drawer. Here's a summary of what's been accomplished:

## Changes Made

### 1. Object Context Actions (Three-Dots Menu)
- **ObjectActionBottomSheet**: Created a shared, reusable bottom sheet component `ObjectActionBottomSheet.kt` that presents context-sensitive actions for files and folders (Preview, Download, Rename, Delete, Properties, etc.).
- **List Items Integration**: Added a "More Options" (`MoreVert`) icon button to all list view variants in the `FileExplorerScreen` (`CompactListItem`, `DetailedListItem`, `FolderItem`, `GalleryCardItem`, `GalleryFolderCardItem`). Clicking it summons the action bottom sheet.
- **Media Viewer Integration**: Embedded the same "More Options" button into the top right of the `MediaViewerScreen`, allowing quick access to properties and actions right from the media carousel.

### 2. Network Layer Refactoring
- **TransferManager**: Built a robust `TransferManager.kt` singleton to centrally track upload and download tasks. It maintains an observable `StateFlow` containing the live status, progress, speed, and metadata for every active or completed transfer.
- **UploadObjectUseCase integration**: Adjusted the network `UploadObjectUseCase` to hook directly into the `TransferManager`, emitting byte-level progress events (currently simulated, ready for AWS SDK byte stream bindings) during uploads.

### 3. Interactive Transfers Drawer
- **TransfersBottomSheet Component**: Designed `TransfersBottomSheet.kt`, a modal drawer listing active and completed transfers with live progress bars, transfer speeds, and status icons.
- **Navigation Update**: Removed the static `Destinations.TRANSFERS` route from `S3Navigation.kt` to favor this new inline component.
- **Drawer Integration**: Wired the "Transfers" entry in the app's `ConnectionDrawerSheet` to launch the `TransfersBottomSheet` directly over the current Explorer context. The list automatically updates itself by collecting `TransferManager`'s state flow through the `FileExplorerViewModel`.

## What was Tested
- **UI Behavior**: Verified that the Context Action sheet correctly receives the selected S3 object data across grid, list, and gallery views.
- **Gradle Build**: Ran a successful `./gradlew assembleDebug` to ensure no regression or cyclic dependencies were introduced by injecting the singleton state models into the ViewModels.

> [!TIP]
> The contextual bottom sheet lays the foundation for future data tasks. While the UI is now fully in place, the dialog actions inside `ObjectActionBottomSheet` (e.g. Delete, Rename, Folder Stats) are currently stubs and ready to be implemented in a follow-up task.
