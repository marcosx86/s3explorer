# Goal Description

Implement the revised refinements to the connection management flows based on the updated `specs/20-connection-management-refinements.md`. The focus is strictly on adding connection validation and removing the extraneous "Create Bucket" button.

## Proposed Changes

### `net.m21xx.s3explorer.ui.connection.NewConnectionScreen`
- **Validation**: 
  - Add a "Validate" button (e.g. an `OutlinedButton` or `TextButton`) alongside the existing "Connect" button.
  - The "Validate" button tests the connection parameters against the S3 endpoint without saving the profile.
  - We can update the UI state to show a "Valid connection!" success message upon successful validation.
- **Bucket Name Field**: 
  - Remove the "Create bucket" (plus icon) `IconButton` from the `trailingIcon` of the Bucket Name text field.

### `net.m21xx.s3explorer.ui.connection.NewConnectionViewModel`
- Keep `testConnection()` as the validation method (since it fetches buckets and doesn't explicitly save, wait... actually `testConnection()` currently calls `saveConnectionProfileUseCase`).
- Split the logic: 
  - `validateConnection()` will call `fetchAvailableBucketsUseCase` to test connectivity.
  - `saveConnection()` will call `saveConnectionProfileUseCase` and return the `profileId`.

## Deferred Items
As requested, the Bucket Listing Modal combo-box and Bulk Import feature have been recorded in the spec as an amend, and will not be implemented at this time. All fields will remain in their original order, and no "Advanced" section will be created.

## Verification Plan
### Manual Verification
- Check that the "plus" button is removed from the Bucket Name field.
- Verify that clicking the "Validate" button accurately tests the S3 connection and reports success/failure without saving the profile to the database.
- Verify that clicking "Connect" successfully saves the profile.
