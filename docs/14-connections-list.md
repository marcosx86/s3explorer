Our connections screen have this aspect, giving the option to reuse connection details (that will jump back to home screen with everything filled in).

---

This "Accounts" screen acts as the identity manager for the application. The ability to manage multiple endpoints and easily duplicate configurations is a massive quality-of-life feature, especially for users juggling personal, work, and backup buckets.

The visual cue of highlighting the active connection in green is an excellent UX touch. Let's map out the architectural requirements to support this multi-tenant view and the specific actions in that context menu.

### 📱 UI Elements Extraction

* **Top App Bar:**
* **`+` Icon:** Navigates directly to a blank connection setup screen.
* **Global Overflow (`⋮`):** Likely for global actions like importing/exporting the entire account list backup.


* **Account List Items:**
* **Visual State:** The currently active profile (`mega-transfers`) is styled in green, providing immediate context.
* **Data Display:** Shows the user-defined alias or bucket name, the access key/username (`admin`), and the endpoint URL.
* **Quick Actions:** A dedicated trash can icon for rapid deletion, and an overflow menu (`⋮`) for advanced profile actions.


* **Context Menu (The Bottom Sheet/Dropdown):**
* **Set custom name:** Allows the user to assign a friendly alias (e.g., "Personal Photos") instead of a raw bucket name.
* **Show .ini config:** A power-user feature that likely formats the connection details into a standard `rclone.conf` syntax.
* **Reuse connection details:** Clones the endpoint and credentials, navigating the user back to the Connect screen to easily add a second bucket from the same server.



---

### ⚙️ Functional Requirements: Service & Data Layers

To make this screen fluid and secure, we have to interact carefully with the encrypted storage we established in the very first step.

#### 1. Data Layer (The Profile Roster)

* **`ObserveAllProfilesQuery` (Room):** * The `ConnectionProfileDao` needs a query that returns a `Flow<List<ConnectionProfileEntity>>`. This ensures that if an account is added, renamed, or deleted, this screen updates reactively without manual refreshes.
* **State Tracking:** * The `ConnectionProfileEntity` must include an `isActive` boolean (or the `GlobalPreferencesDataStore` must store an `activeProfileId`). The UI uses this boolean to apply the green text styling.

#### 2. Service/Domain Layer (The Action Handlers)

This screen triggers several distinct business operations:

* **`SwitchActiveAccountUseCase`:**
* *Trigger:* User taps on a non-active account row (e.g., `patreon-dailydreamsai`).
* *Action:* Updates the local database/preferences to mark the new account as active. Crucially, this must emit a global event that clears the `FileExplorerState` memory cache and forces the main `ObserveDirectoryContentUseCase` to re-sync using the new credentials.


* **`SetCustomNameUseCase`:**
* *Action:* Updates the `alias` column in the Room database for the specific `profileId`.


* **`GenerateRcloneConfigUseCase`:**
* *Action:* Fetches the `ConnectionProfileEntity` from Room and decrypts the Secret Access Key from the Android Keystore. It then formats a string matching the `[alias] type = s3 env_auth = false access_key_id = ...` structure and passes it to the UI layer to be displayed in a dialog or copied to the clipboard.


* **`DeleteProfileUseCase`:**
* *Action:* As outlined in the Settings step, tapping the trash icon must trigger a cascading delete of the Room data, the encrypted Keystore credentials, and any local cached files tied exclusively to that profile.



#### 3. State Management & Navigation (ViewModel)

The "Reuse connection details" feature requires a specific navigation strategy to maintain security.

* **Secure Parameter Passing:** * When "Reuse connection details" is tapped, the app must navigate back to the initial `ConnectRoute` (Screenshot 1).
* *Security Constraint:* You should **not** pass the decrypted Secret Access Key as a plaintext argument in the Jetpack Compose navigation route string (e.g., `connect?secret=12345`), as navigation backstacks can be logged or intercepted.
* *Implementation:* The `AccountsViewModel` should fetch the decrypted credentials, temporarily place them in a shared state holder or a secure `SavedStateHandle` injected into the `ConnectViewModel`, and then trigger the navigation event. The `ConnectViewModel` then consumes this state to pre-fill the username, password, and endpoint fields, leaving the bucket name blank for the user to fill or query.