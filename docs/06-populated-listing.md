Here is another bucket, now populated.

---

This is where the architecture really gets put to the test. A populated list brings up challenges like memory management, asynchronous media loading, and efficient data diffing.

Let's break down the new UI elements introduced in this populated state and map out the functional requirements to keep the app highly performant.

### 📱 UI Elements Extraction

* **List Items (Folders):** * Standard yellow folder icon.
* Displays only the prefix/folder name (e.g., `S02E11_Eira_05_Studio_Premium...`).


* **List Items (Standard Files):**
* Dynamic file-type icons (e.g., a document icon with a green "ZIP" badge).
* **Metadata Row:** Displays a localized timestamp (`Wed, 17 Jun 2026 12:36:31`) and human-readable file size (`293.38 MB`).


* **List Items (Media Files):**
* **Rich Thumbnail:** The `.mp4` file displays an actual image frame instead of a generic icon.


* **Item Actions:**
* **Overflow Menu (`⋮`):** Present on every row, indicating a context menu for file/folder-specific operations (like rename, delete, download, or share).



---

### ⚙️ Functional Requirements: Service & Data Layers

To handle this smoothly, we need to expand our Single Source of Truth (SSOT) to manage pagination, data transformation, and heavy asynchronous operations like thumbnail generation.

#### 1. Data Layer (Pagination & Media Caching)

* **S3 Pagination Handling:** S3's `ListObjectsV2` API returns a maximum of 1,000 keys per request.
* **`ContinuationToken` storage:** The database or a session state manager needs to store the `NextContinuationToken` returned by S3.
* **Append vs. Replace:** When syncing, the repository must know whether to wipe the current directory cache (e.g., on a pull-to-refresh) or append to it (when scrolling down).


* **Media Thumbnail Cache (Disk & Memory):** Fetching thumbnails for large remote `.mp4` files is expensive.
* The app should use an image loading library (like Coil or Glide) configured to aggressively cache these thumbnails on the local disk.
* For remote video files, the `S3NetworkDataSource` might need to utilize HTTP Range requests to fetch only the first few megabytes of the file to extract a frame, preventing a full 334 MB download just to render an icon.



#### 2. Service/Domain Layer (Data Transformation & Actions)

The raw data from S3 is not user-friendly. We need Use Cases dedicated to formatting.

* **`FormatFileMetadataUseCase`:**
* *Input:* Raw S3 `size` (in bytes) and `lastModified` (ISO 8601 string or Date object).
* *Action:* Converts bytes to KB/MB/GB depending on the magnitude. Localizes the date string to match the user's device timezone and format preferences.


* **`ExecuteFileActionUseCase`:**
* Triggered by the `⋮` menu.
* Depending on the user's selection, this routes to specific operations:
* *Delete:* Marks the `S3ObjectEntity` as `PENDING_DELETE` in Room and hides it from the UI immediately, then fires a background worker to execute the S3 `DeleteObject` command.
* *Share:* Generates a temporary S3 Pre-signed URL valid for a set duration, then hands it to the Android `ShareSheet`.





#### 3. State Management & UI Binding (ViewModel)

* **Lazy Loading / Infinite Scroll:**
* The ViewModel must expose a mechanism (like a `PagingData` flow if using Android's Paging 3 library, or a custom scroll listener) that detects when the user is approaching the bottom of the list.
* Upon triggering, it requests the `ObjectRepository` to fetch the next batch using the stored `ContinuationToken`.


* **Asynchronous Diffing:** * Because the UI is observing a Room database table, when a background sync finishes and inserts 1,000 new rows, the UI thread could freeze if not handled correctly.
* The list component (e.g., Compose `LazyColumn` or XML `RecyclerView` with `DiffUtil`) must compute list differences on a background thread to ensure scrolling remains at 60+ FPS while new thumbnails pop in.



If the user clicks that `⋮` overflow menu or selects a file to download, what does the interface look like next?