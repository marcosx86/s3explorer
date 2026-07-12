# S3 Explorer Environment Setup and Initial Implementation Plan

This plan outlines the initial steps to setup the Android project and establish the architectural foundation based on the consolidated functional requirements.

## Open Questions

- What is the minimum Android SDK version we should target? (API 26 recommended for better TLS/crypto support).
- Do we have any pre-existing Android project files in `c:\git\s3explorer`, or should I bootstrap a completely new project in this directory?

## Proposed Changes

### Project Initialization

- Run Android CLI tools or Gradle to bootstrap a new Kotlin-based Android project in `c:\git\s3explorer`.
- Setup standard `build.gradle.kts` configuration.

### UI & Architecture Decisions
- **UI Framework:** Jetpack Compose will be used. We will aggressively use `derivedStateOf` and `remember` for the file list sorting and filtering to prevent unnecessary recompositions when scrolling large buckets.
- **S3 Client SDK:** `aws-sdk-kotlin` will be used as it is natively built on Kotlin Coroutines and Ktor, non-blocking, uses fewer threads, and drastically reduces memory overhead during large multipart uploads.

### Dependency Management

Add the following core dependencies to the project:
- **Core:** Kotlin Coroutines.
- **Local Storage:** Room Database, Preferences DataStore.
- **Background Processing:** WorkManager.
- **Network & Storage:** `aws-sdk-kotlin`.
- **UI & Media:** Jetpack Compose, Navigation Compose.
- **Security:** AndroidX Security Crypto (for EncryptedSharedPreferences).

> [!TIP]
> **Performance Multipliers**
> - **Paging 3 (`androidx.paging`):** Room and Compose are fast, but loading 50,000 `S3ObjectEntity` rows into memory at once will cause OOM crashes or severe frame drops. Paging 3 acts as the bridge, loading data from Room in chunks of 50-100 as the user scrolls, keeping memory usage flat regardless of bucket size.
> - **Dependency Injection (Hilt/Dagger):** For a decoupled architecture to perform well, objects like the `S3NetworkDataSource` or `CryptographicKeyManager` should be singletons. Manually instantiating Use Cases consumes CPU cycles. Hilt ensures they are compiled and injected efficiently.
> - **Coil Optimizations (For Media):** Coil must be configured with a strict DiskCache policy. Fetching S3 pre-signed URLs for image thumbnails is a heavy operation; the UX relies on Coil caching those thumbnails locally so scrolling back up the list is instantaneous.

### Base Architectural Scaffolding

Define the following initial directory structure and base classes:
- **`data/local/`**: `AppDatabase`, `S3ObjectEntity`, `ConnectionProfileEntity`.
- **`data/remote/`**: `S3NetworkDataSource`.
- **`domain/`**: Base Use Case interfaces.
- **`ui/theme/`**: Color palettes, typography.
- **`di/`**: Hilt modules for injecting repositories and S3 clients.

## Verification Plan

### Automated Tests
- Gradle sync and successful project compilation.
- Basic unit test verifying the injection of a Use Case into a ViewModel.

### Manual Verification
- Launching the blank "Hello World" app on an emulator/device to verify the dependencies and build configuration are correct before diving into UI implementation.
