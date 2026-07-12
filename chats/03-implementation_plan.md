# S3 Explorer UI: Phase 2 Implementation Plan (Data Persistence)

This plan outlines the architecture for saving S3 connection profiles. As discussed in our earlier analysis, we must adhere to a strict Single Source of Truth pattern while ensuring credential security.

## User Review Required

> [!CAUTION]
> **Secret Key Storage:** I will use Jetpack's `EncryptedSharedPreferences` to store the Secret Access Key, keyed against the profile ID. This leverages the Android Keystore securely under the hood without exposing raw cryptographic logic. The Room database will only store non-sensitive metadata (Endpoint, Access Key, Bucket Name).

## Open Questions

- `EncryptedSharedPreferences` requires API 23+, and we are targeting minSdk 26, so compatibility is perfect. Is this approach acceptable to you, or do you require a custom KeyStore implementation?

## Proposed Changes

### 1. Database Entities & DAOs (`data/local/`)
- **[NEW]** `entity/ConnectionProfileEntity.kt`: Data class for Room. 
  - Fields: `profileId` (UUID, Primary Key), `alias`, `endpointUrl`, `accessKey`, `defaultBucket`.
- **[NEW]** `dao/ConnectionProfileDao.kt`: Interface with `insert`, `getAll`, `getById`, and `delete` methods.
- **[NEW]** `AppDatabase.kt`: Abstract class extending `RoomDatabase` tying the entities and DAOs together.

### 2. Secure Storage (`data/local/security/`)
- **[NEW]** `SecureStorage.kt`: A helper class wrapping `EncryptedSharedPreferences` for storing/retrieving the `secretKey` using the `profileId` as the preference key.

### 3. Repository Layer (`data/repository/`)
- **[NEW]** `ConnectionRepository.kt`: The single source of truth for connections. It will coordinate saving the `ConnectionProfileEntity` to Room and the `secretKey` to `SecureStorage` in a single transaction.

### 4. Dependency Injection (`di/`)
- **[NEW]** `DatabaseModule.kt`: Hilt module to provide the `AppDatabase`, `ConnectionProfileDao`, and `SecureStorage` singletons.
- **[NEW]** `RepositoryModule.kt`: Hilt module to bind `ConnectionRepository`.

### 5. Domain & UI Integration
- **[NEW]** `SaveConnectionProfileUseCase.kt` (in `domain/`): Use case to generate the UUID, format the entity, and call the repository.
- **[MODIFY]** `NewConnectionViewModel.kt`: Update the `testConnection()` flow to invoke the `SaveConnectionProfileUseCase` upon success, effectively persisting the credentials before navigating to the File Explorer.

## Verification Plan

### Automated Tests
- **[NEW]** `ConnectionProfileDaoTest.kt`: Basic Android instrumented test to verify Room insertion and querying.

### Manual Verification
- Deploy the app to the emulator, input connection data, and click "Connect". 
- Use Android Studio's **App Inspection (Database Inspector)** to visually verify that the `ConnectionProfileEntity` is created in Room and that the Secret Key is **not** present in the database file.
