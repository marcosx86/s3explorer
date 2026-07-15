# Connection Import/Export/Clear Implementation Plan

This plan describes how we will implement the ability to import, export, and completely clear the connections list from the three-dot menu in the `ConnectionsListScreen`.

## User Review Required

- **File Format & Extension:** The exported file will be a Base64-encoded string representing a JSON array of connection profiles (including the encrypted secret key). We will default the MIME type to `application/octet-stream` and suggest a filename like `s3explorer_connections.b64` or `s3explorer_connections.json`. Does this sound correct?
- **Clear List Confirmation:** We will add a confirmation dialog before clearing the list to prevent accidental data loss.

## Proposed Changes

### `net/m21xx/s3explorer/ui/connection/ConnectionsListScreen.kt`
- **[MODIFY]** `ConnectionsListScreen.kt`
  - Add state for a `DropdownMenu` anchored to the global `MoreVert` (three dots) icon.
  - Add `DropdownMenuItem` for **Import**, **Export**, and **Clear List**.
  - Setup `rememberLauncherForActivityResult` with `ActivityResultContracts.GetContent()` (for importing) and `ActivityResultContracts.CreateDocument` (for exporting).
  - Add an `AlertDialog` to confirm the "Clear List" action.

### `net/m21xx/s3explorer/ui/connection/ConnectionsListViewModel.kt`
- **[MODIFY]** `ConnectionsListViewModel.kt`
  - Add `fun clearAllProfiles()` which iterates over all profiles, deletes them from `ConnectionRepository`, and invalidates clients.
  - Add `fun exportConnections(uri: Uri, context: Context)` that retrieves all profiles and their secret keys, creates a list of DTOs, serializes it to JSON using `Gson` or Kotlinx Serialization, encodes it in Base64, and writes it to the `Uri` output stream.
  - Add `fun importConnections(uri: Uri, context: Context)` that reads the Base64 content from the `Uri` input stream, decodes it, deserializes the JSON to a list of DTOs, and saves each profile into `ConnectionRepository`.

### `net/m21xx/s3explorer/data/repository/ConnectionRepository.kt`
- **[MODIFY]** `ConnectionRepository.kt`
  - Ensure there's a method to safely clear all profiles, or we can just iterate through `allProfiles` and call `deleteProfile(it)`.
  - Add a helper method to clear everything if it's more efficient (e.g. `connectionProfileDao.deleteAll()` and clearing all keys in `SecureStorage`), but iterating might be fine depending on the list size.

### `net/m21xx/s3explorer/domain/models/ConnectionExportItem.kt`
- **[NEW]** `ConnectionExportItem.kt`
  - A data class for the JSON representation of the exported connection. We will use this to serialize/deserialize safely.

## Verification Plan
### Automated Tests
- N/A

### Manual Verification
- Create dummy connections in the app.
- Click the three-dots menu -> Export. Select a location to save.
- Verify the saved file is Base64 encoded and contains JSON when decoded.
- Click "Clear List" and accept the prompt. Verify the connections list is empty.
- Click "Import", select the previously exported file. Verify the connections reappear and function correctly.
