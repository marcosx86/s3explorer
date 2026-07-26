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
