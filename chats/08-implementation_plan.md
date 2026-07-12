# Connections List Implementation

This plan outlines the architecture for managing multiple S3 connection profiles, viewing them in a list, and updating the database to support isolated multi-tenant operations.

## User Review Required

> [!WARNING]
> This plan includes a breaking schema change to the local database to support isolated caches per connection profile. Since the migration strategy defaults to destructive, **this will wipe existing connection profiles and cached directory structures.**
> As previously discussed, this is acceptable for now.

## Proposed Changes

---

### Data Layer (Database Schema)

We must update the schema to make `ConnectionProfileEntity` track which profile was used last, and make `S3ObjectEntity` profile-aware so multiple connections don't pollute each other's cache.

#### [MODIFY] [ConnectionProfileEntity.kt](file:///C:/git/s3explorer/app/src/main/java/net/m21xx/s3explorer/data/local/entity/ConnectionProfileEntity.kt)
- Add `lastUsedAt: Long = System.currentTimeMillis()` column.

#### [MODIFY] [S3ObjectEntity.kt](file:///C:/git/s3explorer/app/src/main/java/net/m21xx/s3explorer/data/local/entity/S3ObjectEntity.kt)
- Add `val profileId: String` column as part of the composite key, or retain `objectKey` as the Primary Key but include `profileId` to scope it. Since an object key is just a path (e.g. `images/img1.jpg`), we need a composite primary key: `primaryKeys = ["profileId", "bucketName", "objectKey"]` so different profiles querying buckets with the same name don't conflict.

#### [MODIFY] [AppDatabase.kt](file:///C:/git/s3explorer/app/src/main/java/net/m21xx/s3explorer/data/local/AppDatabase.kt)
- Bump version from 3 to 4.

#### [MODIFY] [ConnectionProfileDao.kt](file:///C:/git/s3explorer/app/src/main/java/net/m21xx/s3explorer/data/local/dao/ConnectionProfileDao.kt)
- Add `@Query` to list all profiles ordered by `lastUsedAt DESC`.

#### [MODIFY] [S3ObjectDao.kt](file:///C:/git/s3explorer/app/src/main/java/net/m21xx/s3explorer/data/local/dao/S3ObjectDao.kt)
- Update queries to filter and delete by `profileId`.

---

### Domain Layer (Action Use Cases)

We need use cases for the context menu actions.

#### [NEW] [DeleteProfileUseCase.kt](file:///C:/git/s3explorer/app/src/main/java/net/m21xx/s3explorer/domain/DeleteProfileUseCase.kt)
- Removes profile from Room, clears `S3ObjectEntity` cache for that profile, and deletes credentials from the Android Keystore.

#### [NEW] [SetCustomNameUseCase.kt](file:///C:/git/s3explorer/app/src/main/java/net/m21xx/s3explorer/domain/SetCustomNameUseCase.kt)
- Updates the `alias` column of a given profile in Room.

#### [NEW] [GenerateRcloneConfigUseCase.kt](file:///C:/git/s3explorer/app/src/main/java/net/m21xx/s3explorer/domain/GenerateRcloneConfigUseCase.kt)
- Retrieves profile and decrypts secret to construct a standard `rclone.conf` format string.

#### [MODIFY] [SyncDirectoryUseCase.kt](file:///C:/git/s3explorer/app/src/main/java/net/m21xx/s3explorer/domain/SyncDirectoryUseCase.kt) & [ObserveDirectoryContentUseCase.kt](file:///C:/git/s3explorer/app/src/main/java/net/m21xx/s3explorer/domain/ObserveDirectoryContentUseCase.kt)
- Update these use cases to scope operations by `profileId`.

---

### UI & Navigation Layer

#### [NEW] [ConnectionsListScreen.kt](file:///C:/git/s3explorer/app/src/main/java/net/m21xx/s3explorer/ui/connection/ConnectionsListScreen.kt)
- A screen built with Jetpack Compose displaying a `LazyColumn` of connections.
- Top app bar with `+` icon to navigate to `NEW_CONNECTION`.
- Context menu for renaming, generating config, duplicating, and deleting.
- Selecting a connection updates its `lastUsedAt` and navigates to the File Explorer.

#### [NEW] [ConnectionsListViewModel.kt](file:///C:/git/s3explorer/app/src/main/java/net/m21xx/s3explorer/ui/connection/ConnectionsListViewModel.kt)
- Coordinates fetching profiles and delegating context menu actions to the Use Cases.

#### [MODIFY] [NewConnectionViewModel.kt](file:///C:/git/s3explorer/app/src/main/java/net/m21xx/s3explorer/ui/connection/NewConnectionViewModel.kt)
- Inject `SavedStateHandle` to retrieve a `reuseProfileId` argument.
- If present, query the decrypted credentials and prefill the username, password, endpoint, and region fields. Leave bucket name empty.

#### [MODIFY] [S3Navigation.kt](file:///C:/git/s3explorer/app/src/main/java/net/m21xx/s3explorer/ui/navigation/S3Navigation.kt)
- Change `startDestination` to the connections list.
- Add `reuseProfileId` as an optional argument to the `NEW_CONNECTION` route.

## Verification Plan

### Automated Tests
- Relying on app execution and Room compiler checks.

### Manual Verification
1. Launch app -> land on Connections List (should be empty initially).
2. Tap `+` -> land on Connect screen.
3. Configure a connection, connect.
4. Back out to Connections List -> verify the newly created connection appears.
5. Use "Reuse connection details" -> verify credentials populate but create a *new* profile on save.
6. Use "Generate config" -> verify rclone config output is correct.
7. Tap the trash icon -> verify it gets deleted completely.
