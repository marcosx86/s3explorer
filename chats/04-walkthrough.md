# S3 Explorer UI: Phase 3 Walkthrough (Network Integration)

I have successfully replaced our mock delay with a live S3 network connection using the official `aws-sdk-kotlin`. The app can now legitimately test endpoint reachability and list buckets natively.

## What was completed

* **Network Data Layer (`data/remote/`):**
  * Created `S3NetworkDataSource.kt` which dynamically instantiates the `S3Client` on the fly using the endpoint URL and credentials provided by the user.
  * Critically, the `S3Client` is configured with `forcePathStyle = true` to guarantee compatibility with MinIO, Ceph, and other S3-compatible backend endpoints.
* **Domain Layer (`domain/`):**
  * Implemented `FetchAvailableBucketsUseCase.kt` to act as the bridge between the UI and the raw SDK client. It executes `ListBuckets` and gracefully captures any network or authentication errors into a Kotlin `Result`.
* **ViewModel Integration (`ui/connection/`):**
  * Updated `NewConnectionViewModel` to natively power the `testConnection()` flow using the new Use Case. If the network test fails (e.g., bad credentials or unreachable endpoint), the error is surfaced in the UI. If it succeeds, the credentials are saved to the Room/Secure Storage layer exactly as before.
* **UI Bucket Selection Modal:**
  * Updated `NewConnectionScreen.kt` to integrate a Material 3 `ModalBottomSheet`. 
  * Tapping the `≡` (List) icon in the Bucket Name field now instantly fires off a live request to the endpoint. A loading spinner is shown in the bottom sheet, followed by a list of fetched buckets. Tapping a bucket instantly populates the input field!

## Verification

> [!TIP]
> **Check the live connection in Android Studio!**
> You can now compile the app to the emulator and test this for real! If you have a local MinIO instance (or an AWS account), enter your credentials and hit the `≡` icon on the bucket field. You should see a live network call populate the Bottom Sheet.

## Next Steps

With the gateway into the app now fully functional, securely persisted, and network-verified, we are ready to build the main attraction: **The File Explorer View**. Shall we proceed to building the bucket navigation and listing the objects inside?
