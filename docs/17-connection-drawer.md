Let's reverse engineer the connection menu drawer, that comes from the upper left menu icon in the bucket explorer activity, where it will lead to settings,  transfers and other options like global about modal with some lorem ipsum text to be defined later.

---

This is a classic, highly effective Android navigation pattern. The Modal Navigation Drawer acts as the command center for the app, seamlessly bridging the context of the *currently active bucket* with *global application controls*.

Notice how the UI is grouped logically: Context (Header) -> Account-specific Actions -> Global Actions -> Destructive Actions -> Contextual Stats (Footer).

Let’s reverse-engineer this drawer and define the architectural requirements to implement it cleanly using Jetpack Compose and our Single Source of Truth (SSOT) architecture.

### 📱 UI Elements & Anatomy Extraction

* **Header (Active Context):**
* **Profile Avatar:** A dynamic circular badge (currently displaying "S3").
* **Title:** The active bucket/profile alias (`telegram-n8n-bot-uploads`).
* **Subtitle:** The endpoint IP or URL (`100.116.0.38`).


* **Menu Group 1 (Account Operations):**
* `Account settings`: Routes to the specific profile configuration (encryption, multipart rules).
* `Transfers`: Triggers the expanding bottom sheet we mapped out previously.


* **Menu Group 2 (Global & Utility Operations):**
* `Sync`: A manual trigger to force-refresh the current directory or the whole bucket metadata.
* `Settings`: Routes to the global device preferences (theme, security, cache).
* `Media backup`: Routes to the auto-upload configuration screen.
* `Trash`: Routes to the local `.trash` prefix explorer.
* `About`: Triggers a simple modal dialog (where we'll inject that placeholder text).
* `Remove credentials`: Triggers the destructive purge confirmation modal.


* **Footer Widget (Storage Stats):**
* **Header:** "Storage" label, relative timestamp ("a few seconds ago"), and a manual refresh `IconData`.
* **Visualizer:** A horizontal progress bar/line separating the header from the data.
* **Data:** Human-readable storage used (`710.13 MB`) and total object count (`files: 1824`).



---

### ⚙️ Functional Requirements: Service & UI Layers

To build this in a decoupled way, the Drawer's UI should be completely "dumb." It shouldn't know *how* to fetch the active profile or *how* to calculate storage; it should only observe state from a ViewModel.

#### 1. UI Layer Implementation (Jetpack Compose)

In Compose, this is built using the `ModalNavigationDrawer` component wrapping a `ModalDrawerSheet`.

* **State Observation:** The Drawer needs to observe a combined `DrawerUIState` data class from the ViewModel:
* `activeProfile: ConnectionProfileEntity?`
* `storageStats: StorageStatsSummary?`
* `isSyncing: Boolean`


* **The "About" Modal:**
* When the "About" row is clicked, the UI state toggles a boolean (`showAboutDialog = true`).
* This triggers a standard Compose `AlertDialog` or `Dialog` overlaying the screen, containing your `Lorem ipsum...` text, a title, and a "Close" button.



#### 2. Service/Domain Layer (The Action Handlers)

The menu items aren't just navigation links; some execute heavy operations.

* **`CalculateStorageStatsUseCase` (Refined):**
* *Trigger:* Tapping the refresh icon in the footer.
* *Action:* As discussed previously, S3 doesn't natively report folder sizes. This Use Case must execute an asynchronous `ListObjectsV2` loop against the entire bucket (or query the local Room DB if we are aggressively caching the whole bucket tree). It sums the byte sizes, counts the rows, and updates the `storageStats` state along with the current `timestamp`.


* **`ForceSyncUseCase`:**
* *Trigger:* Tapping the "Sync" menu item.
* *Action:* Dispatches a request to the `ObjectRepository` to bypass the local cache and fetch the absolute latest truth from the S3 endpoint for the current prefix, ensuring the Room database is perfectly aligned with the server.


* **Navigation Routing:**
* Items like "Account settings" and "Settings" will dispatch events to your Compose `NavController` (e.g., `navController.navigate("route_account_settings/${activeProfileId}")`), sliding the drawer closed as the new screen pushes onto the stack.



#### 3. Data Layer (Relative Time Formatting)

* **Timestamp Handling:** The "a few seconds ago" text means the database needs to store the exact `Long` (Unix epoch) of the last successful storage calculation. A UI formatting utility function (like Android's `DateUtils.getRelativeTimeSpanString`) will convert that raw timestamp into the human-readable string on the fly during recomposition.

With the navigation drawer mapped, you now have the complete skeleton of the main file explorer activity. You've got the top app bar, the dynamic list view, the bottom navigation, the context menus, the transfer manager, and now the side drawer.

Are we ready to start mapping out the specific Compose code structures for this, or is there another configuration screen you want to document first?