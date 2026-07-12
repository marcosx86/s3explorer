Moving back to the item actions, here is the "Properties" action for files and folders. The folders "Stats" just pops up a small white banner with the totalization of the contents.

---

These screens highlight a fundamental difference between standard file systems and object storage like S3. In a traditional OS, calculating a folder's size or fetching file headers is a fast, local operation. In S3, these actions require distinct, sometimes expensive, API calls.

The design choice to hide folder stats behind a manual "Stats" button is an excellent architectural decision for S3. Let's break down the UI elements and the backend logic required to power these property views.

### 📱 UI Elements Extraction

* **File Properties Modal:**
* **Header:** Full file name (`S01E35_Rhea_03_Citadel_Premium_Experience_8k.zip`).
* **Core Metadata:** `ETag` (the object's hash), exact size in bytes alongside a human-readable format, and timestamps for Uploaded/Modified.
* **S3 Headers:** Deep, raw HTTP headers returned by the bucket, including `content-type` and specific Amazon/S3 tracing and metadata headers (`x-amz-request-id`, `x-amz-meta-md5chksum`).


* **Folder Properties Modal:**
* **Header:** Full folder prefix.
* **Action:** A solitary "Stats" text button. Unlike the file modal, there is no immediate metadata.


* **Folder Stats Banner (Snackbar):**
* A transient UI element appearing at the bottom of the screen.
* Displays the computed result: count of files, count of sub-directories, and total storage footprint in both human-readable and raw bytes.



---

### ⚙️ Functional Requirements: Service & Data Layers

To support these modals, our app must differentiate between data we already have cached in the Room Database and data that requires a fresh network trip.

#### 1. Data Layer (Caching vs. Fresh Fetching)

* **`HeadObject` API Integration:**
* When the user opens the *File* Properties modal, the local Room Database (`S3ObjectEntity`) can instantly provide the Name, Size, ETag, and Modified Date.
* However, the deep `x-amz-*` headers and custom metadata (like the MD5 checksum) are *not* returned by the standard directory listing sync. The `S3NetworkDataSource` must execute an S3 `HeadObject` request, which fetches only the headers of the file without downloading the file payload itself.



#### 2. Service/Domain Layer (Use Cases)

* **`FetchFilePropertiesUseCase`:**
* *Action:* Immediately emits the locally cached `S3ObjectEntity` data to populate the top half of the modal. Concurrently fires a `HeadObject` network request.
* *Result:* Once the network call completes, it emits a secondary update containing the raw HTTP headers, filling out the bottom half of the modal asynchronously.


* **`CalculatePrefixStatsUseCase`:**
* *Trigger:* The user taps the "Stats" button inside the Folder Properties modal.
* *The S3 Reality:* S3 does not know how big a "folder" is because folders don't exist.
* *Action:* This Use Case must execute a `ListObjectsV2` loop against the S3 endpoint, using the folder's name as the `Prefix`. It iterates through all pages of results (handling the `ContinuationToken`), keeping a running tally of `totalBytes` and `fileCount`. It identifies "directories" by counting keys that end with the delimiter (usually `/`).
* *Output:* Returns a `PrefixStatsSummary` data class.



#### 3. State Management & UI Binding (ViewModel)

* **Transient UI Events (The Snackbar):**
* Calculating a massive folder's stats might take a few seconds of network pagination. The ViewModel should close the Folder Properties modal as soon as "Stats" is clicked and optionally show a small loading indicator in the app bar.
* When `CalculatePrefixStatsUseCase` finishes, the ViewModel emits a "Side Effect" (e.g., using a Kotlin `Channel` or `SharedFlow` designed for one-time events).
* The UI layer listens for this one-time event and triggers the Android `Snackbar` (the white banner at the bottom) to display the "113 files and 0 directories..." message. This ensures that if the user rotates their screen while the stats are calculating, the result isn't lost, and the snackbar isn't accidentally shown twice.