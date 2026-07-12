This will now be huge, there is a load of options involved in both S3 bucket interaction, local file interaction, UI configuration, and security/encryption. Observe that there is a top label that corresponds to the side drawer, so careful analyzing all screenshots since not all of them are handled by the same screen.

---

This is a massive and incredibly well-thought-out configuration surface. The distinction between "Account settings" (tied to a specific S3 connection) and "Global settings" (tied to the device/app installation) is exactly how a decoupled architecture should handle multi-tenant or multi-profile applications.

Let’s break this down systematically. To maintain a clean architecture, we will route these settings into two separate local storage domains: a `GlobalPreferencesDataStore` and a dynamically instantiated `ProfilePreferencesDataStore` (keyed by the active S3 profile ID).

Here are the functional requirements and architectural implications for these screens.

### 1. 🔐 Account Settings (Profile-Specific Data Layer)

These settings dictate *how* the app interacts with the specific S3 endpoint. Because a user might have a local, unencrypted MinIO bucket and a highly secure, E2E-encrypted AWS bucket, these preferences must be strictly isolated by `profileId`.

#### A. Cryptography & Privacy (E2E Encryption)

The E2E modal reveals that the app relies on Rclone-compatible client-side encryption. This is an architectural heavyweight.

* **`CryptographicKeyManager`:** A service that handles password derivation (e.g., PBKDF2/Argon2). It must securely store the derived encryption keys in the **Android Keystore**, never in plain text.
* **`E2EInterceptor` / `CipherStream`:** Before any file is passed to the `S3NetworkDataSource` for upload, it must be piped through an encrypting stream.
* **`FilenameObfuscationUseCase`:** If "Filename encryption" is toggled, the app must encrypt object keys *before* calling S3 APIs like `PutObject` or `DeleteObject`, and decrypt them instantly when receiving a `ListObjectsV2` response so the UI shows human-readable names.

#### B. S3 Network & Transfer Tuning

The sliders for multipart uploads and concurrent transfers directly govern the `WorkManager` configuration we discussed earlier.

* **`UploadStrategyResolver`:** A Use Case that intercepts an upload request. It reads the "Start threshold (MB)" preference. If the file is smaller, it issues a standard `PutObject`. If larger, it initiates an `CreateMultipartUpload` request.
* **Dynamic Worker Constraints:** The "Upload transfers" slider sets the maximum concurrency. The app must configure `WorkManager` initialization or use a custom `CoroutineDispatcher` with a strictly limited parallelism thread pool (e.g., `Dispatchers.IO.limitedParallelism(userSetting)`).
* **`CalculateMD5UseCase`:** If toggled, the app must stream the entire file locally to calculate its MD5 hash to append to the HTTP headers before uploading, ensuring data integrity at the cost of pre-processing time.

#### C. Cache Lifecycle Management

* **`ClearCacheUseCase`:** Executing "Clear document cache" or "Clear thumbnail cache" triggers a recursive deletion of the app's `Context.cacheDir` specific subfolders, followed by a broadcast to the image loader (like Coil) to wipe its memory cache.

---

### 2. 🌍 Global Settings (App-Wide Data Layer)

These preferences dictate the app's behavior on the specific Android device, regardless of the active S3 bucket.

#### A. System Integration & OS Mounting

* **`StorageAccessFrameworkProvider` (Enable Mount):** This is a critical Android component. By enabling this, the app registers a `DocumentsProvider`. This tells the Android OS that S3Drive is a legitimate file system, allowing the user to open the native Android "Files" app and browse their S3 bucket seamlessly. This requires translating standard Android SAF queries into our localized Room Database queries.
* **`SSLContextFactory`:** "Trust SSL certificate (PEM)" implies the app must dynamically build a custom `TrustManager` for the HTTP client (like OkHttp) to accept self-signed certificates, which is highly common for self-hosted NAS or MinIO setups.

#### B. App Security (Lock Screen)

* **`BiometricAuthenticationManager`:** If "Enable native lock screen" is active, the app registers `ActivityLifecycleCallbacks`. Whenever the app goes to the background and returns to the foreground, it intercepts the UI and throws up the `BiometricPrompt` API, blocking access to the `FileExplorerState` until the user authenticates.

#### C. View Preferences

* **Reactive UI Flows:** Settings like "Display long date format," "Hide dotfiles," and "Show image thumbnails" are saved in `GlobalPreferencesDataStore`. The ViewModel observing the file list combines the Room database query with these preferences.
* *Example:* If "Hide dotfiles" is toggled, the SQLite query automatically appends `AND objectKey NOT LIKE '.%'`. The UI reacts instantly without a network call.



---

### 3. 🗑️ Contextual Screens: Trash & Wiping Data

#### A. The Trash Screen

As noted in the empty state, "Trash is empty."

* **`TrashObservationUseCase`:** Since S3 lacks a native trash bin, this screen simply observes the local Room database using a strict filter for the designated trash prefix (e.g., `SELECT * FROM s3_objects WHERE objectKey LIKE '.s3drive_trash/%'`). It reuses the exact same UI components (RecyclerView, Multi-select action bar) as the main file explorer, just with restricted actions (Restore, Permanently Delete).

#### B. Remove Credentials (The Nuke Option)

The modal to safely delete saved S3 credentials must be treated as a strict database transaction.

* **`PurgeProfileUseCase`:** When the user taps "Remove", the app must execute a cascading delete:
1. Cancel all active `WorkManager` jobs associated with this `profileId`.
2. Delete the `ProfilePreferencesDataStore` file from the disk.
3. Delete all cached media and temporary download files tied to this profile in the local file system.
4. Remove the encryption keys and secret access keys from the Android Keystore.
5. Finally, delete the `ConnectionProfileEntity` and all its child `S3ObjectEntity` rows from the Room database.



### Architectural Summary

By decoupling these layers, your UI components remain incredibly "dumb" and fast. The settings screens simply write booleans and integers to `DataStore`. The Domain Use Cases listen to those `DataStore` changes, dynamically adjusting their behavior (like enabling E2E interceptors, changing UI formatting, or modifying network concurrency) without needing to rewrite or reload the core application logic.