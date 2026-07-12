Here are the options for a folder item. Also a screenshot for file item.

---

These bottom sheets are where the user intent translates into heavy S3 operations. The difference between the File and Folder menus highlights a crucial aspect of S3 architecture: S3 doesn't natively understand "folders."

Let's break down the UI elements and the robust functional requirements needed to bridge the gap between user expectations (standard file system) and S3 realities (flat object store).

### 📱 UI Elements Extraction & Comparison

Both bottom sheets act as context menus, featuring a header with the item's icon and full name.

**Common Actions:**

* **Share link:** Generate a URL for others to access.
* **Make available offline:** Pin the item for local access without an internet connection.
* **Rename:** Change the object key.
* **Move / Copy:** Relocate or duplicate the object.
* **Delete:** Remove the object.
* **Properties:** View deep metadata (eTag, exact byte size, storage class).

**File-Specific Actions:**

* **Open with:** Hands the file over to another Android application via Intent.
* **Show versions:** Queries S3 object versioning history.
* **Download:** Standard file download.

**Folder-Specific Actions:**

* **Download (zip):** Downloads all objects under the prefix and archives them.

---

### ⚙️ Functional Requirements: Service & Data Layers

Because S3 is a flat structure, actions that seem simple to a user (like renaming a folder) require complex orchestration in the background.

#### 1. Data Layer (WorkManager & Background Execution)

UI interactions here should almost exclusively delegate to Android's `WorkManager`. If a user renames a folder containing 5GB of data, the app cannot block the main thread or die if the user swipes the app away.

* **`TransferWorker`:** A generic worker class to handle heavy lifting. It should take parameters for the operation type (`DOWNLOAD`, `COPY`, `OFFLINE_SYNC`).
* **Offline Storage Strategy:** * For "Make available offline," the downloaded files should be stored in the app's internal storage (`Context.getFilesDir()`) to prevent other apps from modifying them, maintaining the integrity of your Single Source of Truth.
* The `S3ObjectEntity` in Room needs an `isPinnedOffline` boolean and a `localUri` pointing to the downloaded file.



#### 2. Service/Domain Layer (The S3 Illusion)

This layer translates file system concepts into S3 API calls.

* **`RenameObjectUseCase` & `MoveObjectUseCase`:**
* *The S3 Reality:* S3 does not have a `Rename` or `Move` API. To rename `file.txt` to `new.txt`, you must perform a `CopyObject` (from old key to new key) and then a `DeleteObject` (on the old key).
* *Folder Complexity:* If the user renames a folder, the Use Case must query `ListObjectsV2` for all items with that prefix, execute a Copy-then-Delete for *every single file*, and emit progress updates to the UI.


* **`ToggleOfflineSyncUseCase`:**
* Marks the `S3ObjectEntity` as `SYNCING` in the local Room database (which instantly updates the UI to show a sync icon).
* Enqueues a `WorkManager` task to download the object(s) and save them to internal storage.


* **`GenerateShareLinkUseCase`:**
* Uses the S3 SDK to generate a Pre-signed URL. It should accept an expiration parameter (e.g., valid for 1 hour, 7 days) and return a standard `https://...` link that can be passed to the Android `ShareSheet`.


* **`FetchObjectVersionsUseCase`:**
* Calls the `ListObjectVersions` S3 API. This requires the bucket to have versioning enabled. It should return a list of historical states for that specific file, allowing the user to restore or download older iterations.


* **`ZipAndDownloadPrefixUseCase`:**
* Triggered by "Download (zip)" on a folder.
* This is an expensive local operation. The Use Case must stream the downloads of all files in the prefix directly into Android's `ZipOutputStream` to avoid out-of-memory (OOM) errors, rather than downloading everything first and zipping later.



#### 3. State Management (ViewModel)

* **Action State Emissions:** When an action like "Delete" is tapped, the ViewModel should update the local Room database to optimisticly hide the item (setting its state to `PENDING_DELETE`). This makes the UI feel lightning fast. If the background network call fails, the item reappears with an error state.
* **Intent Handling:** For "Open with," the ViewModel evaluates if the file is already cached offline. If yes, it creates a `FileProvider` URI and fires an Android `ACTION_VIEW` intent. If no, it must first trigger a temporary download to the cache dir before firing the intent.

These context menus are the command center for the app. How does the user track the progress of these heavy operations? Is there a transfer manager or a dedicated "Downloads/Uploads" screen?