This is the main listing of the bucket, with an empty one.

---

This is the core of the app: the file explorer view. An empty state screen is actually the perfect place to start discussing the caching and synchronization architecture because it forces us to define *how* the app knows it's empty (local cache vs. network response).

Let's break down the UI elements and the functional requirements for a highly decoupled, offline-first file explorer.

### 📱 UI Elements Extraction

* **Top App Bar:**
* **Hamburger Menu `≡`:** Opens a navigation drawer (likely to switch between the connection profiles we created in steps 1 & 2).
* **Title & Badge:** "Files" indicating current context, with an "Upgrade" pill (monetization/premium feature trigger).
* **Add Action `+`:** The primary trigger to upload files, create folders, or take photos.
* **Sharing/Users Icon:** Likely for bucket policy management or sharing pre-signed URLs.


* **Secondary Toolbar (View Controls):**
* **Sort Toggle:** Currently sorting by "Name ↑" (ascending).
* **Layout Toggle:** Currently in List view (could toggle to Grid).
* **Selection Tool:** Checkbox to initiate multi-select mode.
* **Search:** Magnifying glass to filter current view.
* **Split/Detail View:** Icon on the far right (two horizontal rectangles) might toggle a side-panel for file metadata.


* **Main Content Area (Empty State):**
* Visual feedback (Folder icon) and a clear Call to Action ("Tap + to add files here").


* **Bottom Navigation Bar:**
* **Files (Cloud):** Current active tab, representing remote S3 storage.
* **Recent (Clock):** Chronological view of activity.
* **Offline (Download):** A dedicated area for files explicitly cached for offline access.



---

### ⚙️ Functional Requirements: Service & Data Layers

To build a resilient app that handles network flakiness, we must use a **Single Source of Truth (SSOT)** architecture. The UI should *never* read directly from the network. It should only observe the local database, while the network asynchronously updates that database.

#### 1. Data Layer (The Local Cache)

S3 is a flat object store. It doesn't have real folders; it uses key prefixes (e.g., `photos/summer/pic.jpg`). The data layer needs to parse this into a hierarchical structure for the UI.

* **`S3ObjectEntity` (Room Database):** * Fields: `objectKey` (Primary Key), `bucketName`, `size`, `lastModified`, `eTag`, `isDirectory` (boolean flag based on trailing slashes), and `parentId` (to build the folder tree locally).
* **`SyncState` Enum:** Crucial for offline support. Fields like `SYNCED`, `PENDING_UPLOAD`, `PENDING_DELETE`.


* **`ObjectRepository`:** The mediator. When requested, it returns a Kotlin `Flow<List<S3ObjectEntity>>` from Room. Concurrently, it makes a `ListObjectsV2` call to S3 and updates Room with the results. Room's reactivity will automatically push the new data to the UI.

#### 2. Service/Domain Layer (Use Cases)

* **`ObserveDirectoryContentUseCase`:**
* *Input:* `bucketName`, `currentPrefix` (e.g., root is `""`, or `"documents/"`).
* *Output:* `Flow<List<S3Object>>`.
* *Action:* Simply queries the local Room database for objects matching the prefix. If the result is empty, the UI shows the empty state you captured.


* **`SyncDirectoryUseCase`:**
* *Triggered By:* App opening, pull-to-refresh, or entering a new folder.
* *Action:* Calls S3 `ListObjectsV2` for the current prefix. Compares the remote `eTag`s with local `eTag`s, inserts new items, updates changed ones, and deletes removed ones in the Room DB.


* **`EnqueueUploadUseCase`:**
* *Triggered By:* Tapping the `+` icon and selecting a file.
* *Action:* This should **not** upload immediately in the foreground. It should insert a placeholder `S3ObjectEntity` into Room with state `PENDING_UPLOAD` (which the UI can immediately display as a grayed-out item or with a progress bar). Then, it enqueues a `WorkManager` job.
* *Worker:* The Android `WorkManager` handles the actual S3 multipart upload. It survives app closures and handles network retries automatically.



#### 3. State Management & Preferences (ViewModel)

* **`FileExplorerState`:** The ViewModel holds the UI state.
* `isLoading`: Boolean (true while `SyncDirectoryUseCase` is running).
* `items`: List of files (empty in this screenshot's case).
* `currentPath`: String representing where the user is in the bucket.


* **Local Preferences (DataStore):** The secondary toolbar settings (Sort order, List vs. Grid view) should be saved locally using Android `DataStore` (Preferences).
* **`UpdateSortPreferenceUseCase`:** Saves the user's choice (e.g., "Size ↓" instead of "Name ↑") so the app remembers their layout across sessions.



This architecture guarantees that when the user taps "Files," the screen loads instantly from the local Room database, showing the "Empty" state or previous files, while silently fetching the latest truth from the S3 endpoint in the background.

Whenever you're ready, show me the next screen (maybe a populated list or an action menu?) and we'll keep mapping it out!