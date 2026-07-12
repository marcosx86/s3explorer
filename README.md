# S3 Explorer for Android

A modern, offline-first Android application designed to act as a robust file explorer for S3-compatible buckets. It features a highly decoupled architecture utilizing local storage for caching, secure credential management, and background processing for reliable file transfers.

## Architectural Pillars

- **Data Layer Separation:** Decoupled UI from network/storage. S3 API calls are abstracted behind a repository pattern.
- **Local Caching (SSOT):** Implements a Single Source of Truth strategy. Metadata is cached locally using Room Database for offline viewing and instant loading.
- **Background Worker Management:** Uses Android `WorkManager` for reliable file uploads, downloads, and background syncs that survive app closures.
- **Preferences & State Management:** Utilizes Android `DataStore` to store global and profile-specific configurations securely.

## Core Features

- **Connection Management:** Support for multiple S3-compatible connections (e.g., AWS S3, MinIO) with secure credential storage via Android Keystore.
- **Bucket & File Navigation:** Breadcrumb navigation, lazy-loaded thumbnails, dynamic grid/list layouts, and fast sorting/filtering backed by local SQLite queries.
- **File & Folder Operations:** Contextual menus for rename, move, copy, delete, share (pre-signed URLs), and offline pinning.
- **Transfer Manager:** Dedicated background transfer manager tracking active uploads/downloads with real-time speed and progress metrics.
- **Advanced Settings:** Support for client-side encryption (E2E), multipart upload tuning, filename obfuscation, and OS integration (Storage Access Framework).

## Project Structure (Proposed)

- **`data`**: Repositories, Room Database Entities, DAOs, DataStore definitions, and Network Data Sources (S3 API).
- **`domain`**: Clean architecture Use Cases isolating business logic (e.g., `SyncDirectoryUseCase`, `CalculateStorageStatsUseCase`).
- **`ui`**: ViewModels, Compose layouts/XML fragments, and UI state classes.
