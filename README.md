# S3 Explorer for Android

A modern, offline-first Android application designed to act as a robust, high-performance file explorer for S3-compatible buckets (such as AWS S3, MinIO, and Cloudflare R2).

Built with Jetpack Compose, Kotlin, and following Modern Android Development (MAD) practices, the application features secure local caching, encrypted credential management, and highly-optimized media thumbnail loading.

---

## Architecture & Technology Stack

The app is built using **Clean Architecture** principles and the **MVVM (Model-View-ViewModel)** design pattern, keeping the UI, business logic, and data layers decoupled.

* **UI Layer:** Built entirely in **Jetpack Compose** using Material 3 guidelines for a clean, modern look. Navigation is handled reactively using Compose Navigation.
* **Local Caching (Single Source of Truth):** S3 bucket metadata is cached locally via **Room Database** (v4). Queries are exposed through **Jetpack Paging 3** for lazy loading and smooth scrolling of directory contents.
* **Secure Credential Store:** AWS access keys and secret keys are stored securely using **Android Cryptography (EncryptedSharedPreferences / Android Keystore)**. 
* **State & Configuration Persistence:** App configurations (e.g., Explorer View Modes) are stored persistently using Jetpack **Preferences DataStore**.
* **Dependency Injection:** Fully modularized and decoupled using **Dagger Hilt** for DI.
* **Network & S3 Client:** Powered by the official **AWS Kotlin SDK** (`aws.sdk.kotlin:s3`) for S3 communication.
* **Media Processing:** Image and video thumbnails are loaded via **Coil**, configured with OkHttp and standard media frame decoders.

---

## Core Features

### 1. Connection & Profile Management
* **Multi-Profile Accounts:** Add, edit, rename, and manage multiple S3 connections.
* **Smart Display Names:** Fallback chain (Custom Alias -> Default Bucket Name -> Endpoint URL) ensuring neat, readable lists without label duplication.
* **Rclone Configuration Exporter:** Decrypts connection details securely and formats them into a standard `.ini` config ready for `rclone`.
* **Credential Reuse:** Quickly pre-fill the connection screen using credential details from an existing profile.
* **Key Shredding on Delete:** Deleting a profile clears all Room caches related to it and shreds the keys from the Android Keystore.

### 2. Multi-Tenant Local S3 Cache
* **Profile Isolation:** Directory caching is indexed via composite primary keys referencing the specific `profileId`. Multiple accounts accessing buckets with identical names (e.g., `test-bucket` on MinIO vs AWS S3) will never experience data crossover or cache pollution.

### 3. File Explorer & Navigation
* **Connection Drawer**: A slide-out navigation panel providing quick access to active profile details, bucket storage stats (iteratively crawled and cached in Room for accuracy), and setup links.
* **Dynamic View Modes**: Toggle seamlessly between four layouts:
  - **Detailed List:** Standard vertical layout with full size and modification details.
  - **Compact List:** Dense vertical list showing file name only.
  - **Gallery Small:** 2-column square grid of media cards.
  - **Gallery Large:** 1-column layout of large square media cards.
* **Scroll State Preservation**: All layout changes occur dynamically inside a single `LazyVerticalGrid`, keeping the scroll position perfectly preserved.
* **Interactive Breadcrumbs**: Tap-to-navigate path indicator allowing quick jumps to any parent directory in the path hierarchy.

### 4. Settings & Configurations
* **Account Settings**: Isolated by `profileId`. Configures filename encryption, multipart upload thresholds, upload concurrency limit, and local cache lifecycle (clear docs/thumbnails cache).
* **Global Settings**: App-wide options including "Trust insecure SSL/TLS certificates" (allows connections to self-signed local NAS/MinIO endpoints), biometric lock screen activation, dotfiles visibility, and date format styling.

### 5. High-Performance Media Thumbnails
* **Automatic Media Classifier:** Utilizes Android's native `MimeTypeMap` to detect supported formats (e.g., `.jpg`, `.png`, `.webp`, `.mp4`, `.mkv`) from extensions, bypassing the need for manual codec lists.
* **Secure Offline Presigning:** Generates temporary 1-hour pre-signed URLs locally (sub-millisecond HMAC calculations) to avoid memory-heavy file streaming.
* **Strict Concurrency Throttling:** Configures Coil with a custom OkHttp `Dispatcher` to restrict network thumbnail requests to a maximum of **5 concurrent connections**, protecting both remote storage and device network bandwidth.
* **Video Frame Extraction:** Registers Coil's `VideoFrameDecoder` and overrides mime type checks to automatically extract and downsample the first frame from remote video streams.
* **Zero-Flicker Scrolling:** Presigned URLs are stored in an in-memory `thumbnailCache` in the ViewModel. When items scroll back into view, they load their URLs synchronously, hitting Coil's memory cache instantly and preventing list flashing.

### 6. Dedicated Video Player
* **Immersive Playback:** Launches videos in a dedicated activity using Media3 (ExoPlayer) with full system bar hiding.
* **Custom Control Overlay:** Single-tap to toggle controls, featuring a red progress bar with live frame-by-frame scrubbing.
* **Smart Timecodes:** Left-aligned time elapsed and total duration labels matching native hour/minute/second patterns.
* **Advanced Player Controls:** Quick action buttons to toggle loop mode (repeating active video), orientation locks, and fullscreen transitions.
* **Swipe to Dismiss:** Support for swipe-down gestures to instantly close the player activity and return to the explorer.

### 7. Branding & Theming
* **Premium UI Palette:** Custom Material 3 color schemes derived from the official branding (Ocean Blue, Sky Blue, Warm Wood).
* **Native Splash Screen:** Seamless launch experience utilizing Android 12's `core-splashscreen` library.
* **Empty State Watermarks:** Desaturated, grayscale, non-intrusive watermark background in empty folders to reinforce branding.

---

## Project Structure

```
net.m21xx.s3explorer/
├── data/
│   ├── local/              # Room DB, DAOs, Entities, Preferences DataStore
│   ├── remote/             # AWS SDK networking wrappers
│   └── repository/         # Data Repositories orchestrating caching & syncing
├── domain/                 # Use Cases (Sync, Presigning, Config Exporter, Profile Save)
└── ui/
    ├── components/         # Reusable UI widgets (e.g. Watermarks)
    ├── connection/         # New Connection & Connections List screens
    ├── explorer/           # S3 File Explorer, View Mode definitions, Grid Items
    └── navigation/         # S3NavHost and application route graphs
```

---

## How to Build & Run

### Prerequisites
* Android Studio (Koala or newer recommended)
* JDK 17
* Android Device/Emulator running Android 8.0 (API 26) or higher

### Building
1. Clone the repository.
2. Build the project using the Gradle wrapper:
   ```bash
   ./gradlew assembleDebug
   ```
3. Run the app directly from Android Studio.

---

## Development Iterations
* Bucket Totalization in Drawer
* Video Player in Media Viewer
* Splash Screen & Premium Theming
