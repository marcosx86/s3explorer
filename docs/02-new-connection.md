First screenshot is about creating new S3-compatible connection, to be stored in a list of know connections, that could be selected by the user, to be presented in another screenshot. Extract all UI elements and elaborate possible components in service and data layers.

---

This is a great starting point. The "Connect with S3 endpoint" screen is the gateway to the application, so handling state, security, and validation correctly here sets the tone for the entire app.

Let's break down the extracted UI elements and map them to the functional requirements across the service and data layers.

### 📱 UI Elements Extraction

Here is the breakdown of the interactive elements detected on the screen:

* **Authentication Inputs:**
* **Access Key ID / Username:** Standard text input field.
* **Secret Access Key / Password:** Masked text input with a visibility toggle (eye icon).


* **Network & Storage Inputs:**
* **Endpoint URL:** URL input field (currently showing a local/VPN IP `http://100.116.0.38:9000`). It includes an end-icon (building) likely used for selecting predefined AWS regions or known host templates.
* **Bucket Name:** A hybrid input field. It acts as a text input but includes action icons (`≡` for listing available buckets using the provided credentials, and `+` for creating a new bucket directly from this screen).


* **Legal & Actions:**
* **Checkbox:** Mandatory Terms of Service and Privacy Policy agreement.
* **Primary Button:** "Connect" to initiate the connection test and save the profile.


* **Navigation & Footer Links:**
* Help/Docs, Sign-in (for cloud-synced accounts), External Provider integrations.
* Footer navigation: Settings, About, Accounts.



---

### ⚙️ Functional Requirements: Service & Data Layers

To maintain a decoupled architecture, the UI should only collect these inputs and pass them to a `ViewModel`, which orchestrates the following backend components.

#### 1. Data Layer (Storage & Network)

This layer handles the actual persistence and external communication.

* **`ConnectionProfileEntity` (Local Database):** A Room Database table to store the saved connections. Fields should include `profileId`, `alias` (optional user-friendly name), `endpointUrl`, `accessKey`, and `defaultBucket`.
* **Encrypted Storage:** The `Secret Access Key` **must not** be stored in plain text in the Room database. It should be securely stored using Android's `EncryptedSharedPreferences` or the Android Keystore system, linked to the `profileId`.
* **`S3NetworkDataSource`:** An abstraction over the S3 client (e.g., AWS SDK for Android or MinIO client). It takes the endpoint and credentials to initialize a temporary client.

#### 2. Service/Domain Layer (Use Cases)

This layer contains the business logic, keeping the UI dumb and the Data layer strictly for storage/retrieval.

* **`TestConnectionUseCase`:** Triggered when the user taps "Connect" or the bucket list icon (`≡`). It instantiates the `S3NetworkDataSource` with the current input fields and performs a lightweight operation (like `ListBuckets`) to verify credentials and endpoint reachability.
* **`FetchAvailableBucketsUseCase`:** Triggered by the `≡` icon. It calls the network data source, retrieves the list of buckets, and passes them back to the UI to populate a dropdown or bottom sheet.
* **`SaveConnectionProfileUseCase`:** Triggered after a successful connection test. It coordinates saving the non-sensitive data to the Room Database and the secret key to the Encrypted Storage, then updates the app's global state to set this as the active connection.

#### 3. State Management & Caching

* **Input State:** The ViewModel should hold the state of the form (e.g., enabling the "Connect" button only if the URL is valid, the access key is present, and the TOS checkbox is ticked).
* **Connection List:** Once saved, this new connection must trigger a reactive update (e.g., using Kotlin `Flow`) to any other screen observing the list of "Known Connections."

Ready for the next piece of the puzzle. What does the screen look like once the user successfully connects and views their files or connection list?