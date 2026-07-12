Let's document other actions of the main explorer screen. Sending now screenshots of view modes, sorting combo box, selection mode, and large icon list mode.

---

These screens reveal the layer of polish that transforms a basic S3 client into a true file explorer. Features like breadcrumb navigation, dynamic view switching, sorting, and multi-selection require a highly responsive UI, meaning our state management needs to be completely detached from heavy background operations.

Let’s break down these UI controls and map out the functional requirements to handle them efficiently.

### 📱 UI Elements Extraction

* **Breadcrumb Navigation (`Files > blemai > 2026`):**
* Replaces the static "Files" title. Allows the user to see their exact depth in the bucket and likely tap higher-level directories to jump back quickly.


* **View & Layout Controls:**
* **Sort Menu:** A dropdown overlay with "DIRECTION" (Ascending/Descending) and "SORT BY" (Name, Size, Type, Last updated, Last modified).
* **Layout Toggle:** An icon (top right) that dynamically switches the main content area between a compact vertical List View and a large-card Grid View.


* **Multi-Selection Mode (Contextual Action Bar):**
* Triggered by the checkbox icon (or a long-press on an item).
* Replaces the standard top bar with a Contextual Action Bar (CAB) showing the count of selected items ("0 Selected"), a "Select All" toggle, and an "X" to dismiss the mode.
* Injects checkboxes into the UI of every list/grid item.



---

### ⚙️ Functional Requirements: Service & Data Layers

To keep the application snappy, UI configuration state (like "Grid View") must be managed separately from the actual S3 file data.

#### 1. Data Layer (User Preferences & Querying)

* **UI State Persistence (`DataStore`):**
* We should not use Room for UI toggles. Android's `Preferences DataStore` is the modern standard for this.
* **`FileExplorerPreferences`:** Stores key-value pairs for `ViewMode` (LIST or GRID), `SortBy` (NAME, SIZE, DATE, etc.), and `SortDirection` (ASC, DESC). This ensures that if the user closes the app in Grid view sorted by Size, it opens exactly the same way next time.


* **Dynamic Room Queries:**
* Sorting a directory with 10,000 items in memory on the UI thread will cause frame drops. Sorting should be delegated to SQLite (Room).
* The `S3ObjectDao` needs a query that accepts the sorting parameters dynamically: `SELECT * FROM s3_objects WHERE parentId = :currentPrefix ORDER BY CASE WHEN :sortBy = 'NAME' THEN objectKey END ASC...` (or constructed via Room's `@RawQuery` / SQLite query builders for cleaner dynamic sorting).



#### 2. Service/Domain Layer (State Mutation)

* **`UpdateExplorerPreferencesUseCase`:**
* Triggered when the user taps a new sort option or the layout toggle.
* *Action:* Writes the new preference to `DataStore`. Because `DataStore` exposes a `Flow`, the ViewModel observing these preferences will react automatically, triggering a re-fetch from Room with the new `ORDER BY` clause or telling the UI to switch to a Grid layout.


* **`MapsBreadcrumbsUseCase`:**
* S3 uses string prefixes (e.g., `blemai/2026/`).
* *Action:* If the user is in `blemai/2026/` and taps `blemai` in the top breadcrumb, this Use Case pops the navigation stack back to `blemai/`, updates the ViewModel's current path, and triggers `ObserveDirectoryContentUseCase` for the new prefix.



#### 3. State Management & UI Binding (ViewModel)

* **Multi-Selection State Machine:**
* The ViewModel must maintain a `Set<String>` of `selectedObjectKeys`.
* **Idle State:** `selectedKeys` is empty. The UI shows standard icons.
* **Selection Mode:** When `selectedKeys.isNotEmpty()` (or the user manually taps the checkbox icon), the ViewModel emits a state change. The UI reacts by sliding in the Contextual Action Bar and animating the appearance of checkboxes on all list items.
* **Select All / Deselect All:** When "Select All" is tapped, the ViewModel grabs the current list of objects in memory and populates the `Set` with all their keys. Tapping "X" simply clears the `Set`.


* **Layout Manager Swapping (UI Layer):**
* If using standard Android XML, the UI observes the `ViewMode` preference and swaps the `RecyclerView.LayoutManager` between `LinearLayoutManager` and `GridLayoutManager` on the fly.
* If using Jetpack Compose, it transitions between `LazyColumn` and `LazyVerticalGrid`.
* *Crucial Detail:* When switching to Grid mode, the image loading library (Coil/Glide) should be configured to load slightly higher-resolution thumbnails from the disk cache, as the images are now much larger than in the compact list view.



With multi-selection mapped out, how do you plan to handle batch operations? If a user selects 50 items and hits "Download" or "Delete," should we route that through the `TransferManager` we designed earlier, or is there a different batch-processing UI you have in mind?