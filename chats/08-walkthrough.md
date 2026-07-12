# Connections List Implementation

I have successfully completed the Connections List feature! This introduces a proper "Accounts" screen to act as the identity manager for the application.

## Changes Made

### 1. Database Migration & Caching Isolation
- Updated `ConnectionProfileEntity` with a `lastUsedAt` column to track the active connection.
- Altered `S3ObjectEntity` to include a `profileId` composite primary key. **This is critical:** It ensures that multiple profiles can query S3 buckets with the same name without corrupting or crossing over each other's cached file hierarchy.
- Incremented the Room database to Version 4 (this triggers the expected destructive migration to clear out the old single-tenant schemas).

### 2. Context Menu Use Cases
I created clean architectural domain use cases for all the context menu actions:
- `SetCustomNameUseCase`: Updates the connection's display alias.
- `GenerateRcloneConfigUseCase`: Decrypts the secure credentials and dynamically generates a standard `rclone.conf` format string.
- `DeleteProfileUseCase`: Executes a cascading delete. It clears the S3 object cache for the specific profile, deletes the profile from Room, and shreds the credentials from the encrypted Android Keystore.

### 3. Connections List Screen
- Built the `ConnectionsListScreen` which is now the new start destination of the app.
- Replaced the initial "Plus" icon plan with the `Group` (two persons) icon in the TopAppBar per your request.
- Profile cards display the alias (falling back to bucket name), access key, and endpoint. The active/last-used profile automatically highlights in green.
- Implemented dropdown menus to execute the custom use-cases.

### 4. Connection Reuse (Duplication)
- Selecting "Reuse connection details" seamlessly bounces the user back to the New Connection screen.
- Modified `NewConnectionViewModel` to detect the `reuseProfileId` argument in the route, fetch the decrypted credentials, and pre-fill the form so users can easily spin up a second bucket under the same server without re-typing their keys.

## Testing Performed
- Validated that the app compiles and builds successfully with the refactored profiles schemas.
- Verified `S3Navigation` properly handles the argument passing (`new_connection?reuseProfileId={id}`).

## Next Steps
You should launch the app! Note that since the database schema bumped to v4 with a destructive migration fallback, your previous test connections will be wiped clean as expected. Add a new connection using the `Group` icon, and then try out the contextual actions!
