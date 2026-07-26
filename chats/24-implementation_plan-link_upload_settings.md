# Linking Upload Settings Implementation Plan

This plan details the integration of the upload-related settings defined in `specs/19-ui-and-app-settings.md` into the `UploadObjectUseCase`.

## Open Questions
- **Skip same file upload (Modified Time)**: S3 automatically sets the `LastModified` date to the time the upload finishes, ignoring the local file's modification time. To compare the local modification time on future uploads, we must explicitly store the local time in S3 custom metadata (e.g., `x-amz-meta-mtime`). Should I implement this custom metadata injection for all uploads moving forward?
- **E2E Encryption Passphrase**: I'll store the E2E encryption passphrase securely in Android's `EncryptedSharedPreferences` alongside the S3 secret key, rather than in Room as plain text. Do you approve this security enhancement?
- **E2E Payload**: AES-GCM requires a unique Initialization Vector (IV) for every encryption. I plan to store the IV in the S3 object's custom metadata (e.g., `x-amz-meta-iv`) so it can be decrypted during download. Does this approach work for you?

## Proposed Changes

### 1. Data Store and Security Layers
#### [MODIFY] `net.m21xx.s3explorer.data.local.preferences.ProfilePreferencesDataStore.kt`
- Add `e2eEncryptionEnabled` property to `ProfilePreferences` data class.
- Add getter and setter functions for `e2eEncryptionEnabled`.

#### [MODIFY] `net.m21xx.s3explorer.data.local.security.SecureStorage.kt`
- Add `savePassphrase(profileId: String, passphrase: String)` and `getPassphrase(profileId: String): String?` methods to securely persist the E2E passphrase.

#### [MODIFY] `net.m21xx.s3explorer.data.repository.ConnectionRepository.kt`
- Wrap and expose the new `SecureStorage` passphrase methods.

### 2. Network Layer
#### [MODIFY] `net.m21xx.s3explorer.data.remote.S3NetworkDataSource.kt`
- Update `uploadObject` signature to accept optional parameters: `storageClass: String?`, `contentMd5: String?`, `metadata: Map<String, String>?`.
- In `PutObjectRequest`, apply these parameters if they are not null/empty (e.g., set `storageClass = aws.sdk.kotlin.services.s3.model.StorageClass.fromValue(storageClass)`).
- Add a new function `headObject(profileId, bucket, key)` to query object metadata to support the "Skip same file upload" rule.

### 3. Domain Layer
#### [MODIFY] `net.m21xx.s3explorer.domain.UploadObjectUseCase.kt`
- Update signature to accept `localModifiedTime: Long`.
- Inject `ProfilePreferencesDataStore` and fetch the profile's settings.
- **Skip Same File Upload**: 
  - If enabled and `fileBytes.size >= 10KB`, use `headObject` to check if the remote object exists.
  - If it exists, compare its `ContentLength` to `fileBytes.size` and its custom `mtime` metadata to `localModifiedTime`. If they match, abort the upload and return early.
- **E2E Encryption**: 
  - If enabled, fetch the passphrase from `ConnectionRepository`. 
  - Generate a secure key and an IV, encrypt the `fileBytes` using AES-GCM. 
  - Add the IV to the S3 metadata map (`x-amz-meta-iv`).
- **MD5 Calculation**: 
  - If enabled, compute the MD5 digest of the payload (encrypted or plain) and Base64 encode it. 
  - Pass it to the network data source.
- **Storage Class**: Pass the configured storage class string to the network data source.

### 4. UI Layer
#### [MODIFY] `net.m21xx.s3explorer.ui.explorer.FileExplorerViewModel.kt`
- Update the `uploadFile` method to extract the `lastModified` attribute from the file using the `ContentResolver` (or `java.io.File`) and pass it down to `UploadObjectUseCase`.

## Verification Plan
### Automated Tests
- The build will be run (`./gradlew assembleDebug`) to ensure no signature mismatches or typing errors exist across layers.

### Manual Verification
- We will visually review the generated use case implementation to ensure E2E encryption and metadata extraction follow standard cryptographic patterns.
