Let's refine the view modes feature of the main bucket navigator.

---

These screenshots introduce a crucial usability feature: dynamic view modes. Users managing photography or media backups need a highly visual gallery view, while users managing documents or heavy folder structures need compact, dense information.

Let's extract the differences between these three screens and map out how to implement this dynamic switching in Jetpack Compose without destroying the underlying data state or causing memory spikes.

### 📱 UI Elements Extraction & View Modes

Based on the three screenshots, the app supports at least three distinct layout configurations:

1. **Gallery / Large Card View (Screenshot 1):**
* **Layout:** Full-width image cards.
* **Data Displayed:** High-resolution thumbnail, object key (filename), and the overflow (`⋮`) menu.
* **Missing:** File size and timestamps are hidden to maximize visual space.


2. **Detailed List View (Screenshot 2):**
* **Layout:** Standard vertical list.
* **Data Displayed:** Medium-sized square thumbnail, object key, localized date/time, and human-readable file size.


3. **Compact List View (Screenshot 3):**
* **Layout:** Dense vertical list.
* **Data Displayed:** Small square thumbnail, object key, and overflow menu.
* **Missing:** The secondary text row (date/size) is completely removed, allowing significantly more files to fit on the screen at once.



---

### ⚙️ Functional Requirements: Service & Data Layers

To handle this cleanly in a decoupled architecture, the `ViewMode` must be treated as a pure UI configuration state, completely separate from the `S3ObjectEntity` data pipeline.

#### 1. Data Layer (State Persistence)

* **`ExplorerViewMode` Enum:** Define an enum representing these states: `GALLERY`, `DETAILED_LIST`, `COMPACT_LIST`.
* **`DataStore` Integration:** As established in the Global Settings, this preference must be saved locally.
* *Action:* When the user toggles the view icon in the top app bar, a `ToggleViewModeUseCase` cycles through the Enum and writes the new value to the `Preferences DataStore`.



#### 2. UI Layer (Jetpack Compose Implementation)

This is where Jetpack Compose shines compared to legacy XML. Instead of swapping out `RecyclerView.LayoutManager`s and invalidating complex Adapters, Compose simply recomposes the list based on the state.

* **State Observation:** The `FileExplorerViewModel` exposes `val viewMode: StateFlow<ExplorerViewMode>`.
* **Dynamic Composables:**
```kotlin
when (viewMode) {
    ExplorerViewMode.GALLERY -> {
        LazyColumn { items(fileList) { file -> GalleryCardItem(file) } }
    }
    ExplorerViewMode.DETAILED_LIST -> {
        LazyColumn { items(fileList) { file -> DetailedListItem(file) } }
    }
    ExplorerViewMode.COMPACT_LIST -> {
        LazyColumn { items(fileList) { file -> CompactListItem(file) } }
    }
}

```


*Note: If the gallery view eventually becomes a multi-column grid, you would swap `LazyColumn` for `LazyVerticalGrid`.*

---

### 🚀 Performance & UX Considerations (The Hidden Traps)

Switching view modes dynamically introduces significant memory and performance challenges, particularly regarding image loading.

#### 1. Adaptive Image Loading (Coil Integration)

If you load a full-resolution 4K image while in `COMPACT_LIST` mode, the app will consume massive amounts of RAM for a tiny 48dp square. If you switch to `GALLERY` mode, stretching that 48dp thumbnail will look blurry.

* **Dynamic ImageRequests:** The UI must pass the target size to the image loader (like Coil) based on the active `ViewMode`.
* *Compact/Detailed:* Request a low-res thumbnail (e.g., `size(128, 128)`). Coil caches this small bitmap.
* *Gallery:* Request a higher resolution bounds (e.g., `size(1080, 1080)`).


* **Crossfade Transitions:** To make the UX feel premium, enable `crossfade(true)` in Coil so that when switching from Compact to Gallery, the small blurry cached image is shown instantly while the high-res version fades in seamlessly.

#### 2. Scroll State Preservation

When a user scrolls down 500 items in `COMPACT_LIST` mode and then switches to `GALLERY` mode, they expect to remain at the same file in the list.

* **`rememberLazyListState()`:** You must hoist the `LazyListState` to the parent Composable. When the `when` block switches the layout between the different list types, passing the *same* `LazyListState` into the new `LazyColumn` ensures the scroll position is perfectly preserved across view mode transitions.

By strictly separating the *data* (fetched via Room/WorkManager) from the *presentation* (managed by DataStore and Compose `when` blocks), adding new view modes in the future becomes incredibly trivial.