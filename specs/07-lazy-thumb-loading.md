Here is another folder, showing that items should be populated with metadata as fast as you can with lazy loading of thumbnails.

---

This screen perfectly captures the "perceived performance" pattern. By decoupling the lightweight metadata from the heavy media payloads, you keep the app feeling incredibly snappy.

Let's break down the UI updates and the specific architectural requirements to achieve that instant-text, lazy-image loading mechanism.

### 📱 UI Elements Extraction

* **Updated App Bar:**
* **Back Navigation (`<-`):** Replaces the root app icon, indicating we are deeper in the navigation stack.
* **Folder Title:** The title dynamically updates to the current folder/prefix (truncated to fit: `..._Studio_Premium_Experience_8k`).


* **Media List Items:**
* **Loading Placeholder:** A distinct visual indicator (the blue curved line) showing where the thumbnail will eventually appear.
* **Text Metadata:** The filename, file size, and timestamp loaded instantly.


* **Persistent Elements:** The secondary toolbar (sort/filter) and bottom navigation remain, confirming this is a state change within the same core "Files" fragment/screen, not a completely new activity.

---

### ⚙️ Functional Requirements: Service & Data Layers

To achieve the behavior where metadata loads "as fast as you can" while thumbnails load lazily, we must strictly separate data streams and handle S3 authentication for media correctly.

#### 1. Data Layer (Instant Metadata & S3 Streams)

* **The "Instant" Text (Room DB):** When the user taps the folder in the previous screen, the `ObserveDirectoryContentUseCase` instantly emits the cached contents of this new prefix from the Room database. Because text is negligible in size, the UI renders the names, dates, and sizes in milliseconds.
* **Thumbnail Caching Strategy:** * **Memory Cache:** High-priority, small LRU cache for images currently on screen.
* **Disk Cache:** Larger cache for previously loaded thumbnails to prevent re-fetching them from S3 when scrolling up and down.



#### 2. Service/Domain Layer (S3 Image Resolution)

This is the trickiest part of an S3 explorer. You can't just pass an S3 object key to a standard image loading library (like Glide or Coil) because the bucket is likely private and requires authentication.

* **`GeneratePresignedUrlUseCase` (The easiest approach):**
* *Trigger:* As a list item scrolls onto the screen, the UI's image loader requests the image.
* *Action:* The app asks the ViewModel for a URL. This Use Case uses the S3 SDK to generate a temporary, read-only URL (e.g., valid for 5 minutes) for that specific `objectKey`.
* *Output:* Passes the temporary `https://...` URL to the image loader to handle the actual downloading and caching.


* **Custom Image Loader Fetcher (The robust approach):**
* Instead of pre-signed URLs, you write a custom `Fetcher` for your image library (e.g., a Coil `Fetcher`) that directly injects your `S3NetworkDataSource`. When Coil needs an image, it asks your data source to stream the bytes directly via the S3 `GetObject` API. This is more secure and avoids URL expiration edge cases.


* **`FormatLocalizedDateUseCase`:**
* S3 returns timestamps in UTC. To ensure the user isn't confused, this Use Case must parse the ISO-8601 string and format it dynamically to the device's current timezone. This guarantees that whether the user is checking files during the day or late at night, the timestamps natively reflect their local time context—like Bahia standard time—without requiring manual timezone offsets.



#### 3. State Management & UI Binding (ViewModel)

* **Prefix Navigation Stack:** S3 doesn't have real folders; it has prefixes (e.g., `folderA/folderB/image.jpg`).
* The ViewModel needs to maintain a `Stack<String>` of the user's path.
* Tapping a folder pushes a new prefix onto the stack (e.g., `"folderA/"` -> `"folderA/folderB/"`).
* Tapping the `<-` Back arrow pops the stack and updates the `ObserveDirectoryContentUseCase` to query the parent prefix.


* **Lazy Loading State Management:** The UI state for each file item needs to support the placeholder. It starts in a `Loading` state, and the image loading library handles the transition to `Success (Bitmap)` or `Error (Fallback Icon)` independently of the main text metadata flow.

With the files now loaded and displayed, what happens when a user wants to interact with one? Do you have a screenshot of the file actions menu (from the `⋮` icon) or the file viewer itself?