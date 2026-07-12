Second screenshot is the bucket selection modal.

---

This screenshot perfectly illustrates the next step in the flow. The user has triggered an action (likely tapping the `≡` list icon from the previous screen) to query the S3 endpoint for available buckets.

Let's break down the functional requirements for this specific modal, keeping our focus on a decoupled architecture.

### 📱 UI Elements Extraction

* **Dialog Component:** A modal overlay blocking interaction with the underlying connection form.
* **Title:** "Select bucket" - indicates the purpose of the dialog.
* **Dropdown/Spinner:** Displays the fetched list of buckets. Currently showing `mega-cunha21xx-jasminex`. The dropdown arrow indicates the user can tap to see other available buckets.
* **Primary Action Button:** "Import all" - A bulk action likely intended to save connection profiles for *every* bucket found on this endpoint.
* **Secondary Action Button:** "Cancel" - Dismisses the dialog without taking action.

---

### ⚙️ Functional Requirements: Service & Data Layers

This modal requires careful orchestration because it bridges live network data (the bucket list) with local persistence (importing them).

#### 1. Data Layer (Network & Local Storage)

* **`S3BucketDTO` (Data Transfer Object):** A lightweight model representing the network response when listing buckets (containing `bucketName` and perhaps `creationDate`).
* **Transient Cache (Memory):** When the `ListBuckets` network call succeeds, the resulting list should be held in memory (within the ViewModel or a transient repository scope) so the user can interact with the dropdown without triggering redundant network calls.
* **Batch Insert Operations (Room DB):** To support the "Import all" feature, the `ConnectionProfileDao` (Data Access Object for Room) needs a `@Insert(onConflict = OnConflictStrategy.REPLACE)` method that accepts a `List<ConnectionProfileEntity>`.

#### 2. Service/Domain Layer (Use Cases)

* **`FetchAvailableBucketsUseCase` (Refined):**
* *Input:* Endpoint URL, Access Key, Secret Key.
* *Action:* Calls the `S3NetworkDataSource` to execute a `ListBuckets` API command.
* *Output:* Returns a `Result<List<String>>` (Success with bucket names, or Error with a specific exception like `Unauthorized` or `HostUnreachable`).


* **`BatchImportProfilesUseCase`:** * *Trigger:* User clicks "Import all".
* *Input:* The current credentials, endpoint, and the full list of fetched bucket names.
* *Action:* Maps the list of bucket names into a list of `ConnectionProfileEntity` objects. Securely stores the secret key *once* (or maps it to a shared alias in the Keystore), and executes the batch insert into the Room database.


* **`SelectSingleBucketUseCase`:**
* *Trigger:* User selects a specific bucket from the dropdown and (presumably) hits a confirmation button (if the UI flow changes to allow single selection confirmation, though right now it only shows "Import all" or "Cancel"). *Note: If the user just wants to connect to the one selected in the dropdown, the UI might need a "Connect" button inside this dialog, or selecting it populates the main form.*



#### 3. State Management & UI Binding (ViewModel)

* **Dialog State Flow:** The ViewModel must emit a state representing this dialog:
* `Idle` (Hidden)
* `Loading` (Showing a spinner while `FetchAvailableBucketsUseCase` runs)
* `Success(buckets: List<String>)` (Populates the dropdown)
* `Error(message: String)` (E.g., "Invalid credentials" – prompts a toast or in-dialog error message).


* **Decoupled Navigation:** When "Import all" is clicked, the ViewModel triggers the `BatchImportProfilesUseCase`. Upon completion, it emits a side-effect (like a Kotlin `Channel` or `SharedFlow` event) to instruct the UI to dismiss the dialog, close the connection screen, and navigate the user to the "Home" or "File Explorer" view.

**Architectural Note on "Import all":** If the user has hundreds of buckets, importing them all might take a moment. The UI should immediately reflect a loading state on the "Import all" button while the `BatchImportProfilesUseCase` runs, preventing double-taps and ensuring data integrity in the local Room database.

Ready for the next screenshot! Let's see where the user lands after a successful connection.