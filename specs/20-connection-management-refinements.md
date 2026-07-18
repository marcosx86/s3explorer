# Connection Management Refinements Spec

This specification outlines the refinements to the connection management flows.

## 1. New Connection Screen Updates
- **Validation**: Add a "Validate" button to test the connection parameters against the S3 endpoint before saving the profile.
- **Bucket Field**: Remove the "plus" (create bucket) button from inside the bucket name field. All fields remain in their current order without an "Advanced" section.

---
## Amend (Deferred for future implementation)

**Bucket Listing and Bulk Import**
- The Bucket selection button will open a modal that contains a combo box with all bucket names.
- The modal will have "Import all" and "Select" buttons.
- Pressing Back or clicking outside should implicitly close the modal, no special implementation should go here.
- If the user selects one bucket, it will fill the input and stay in the form.
- If the user clicks "Import all", it will iterate over all items, upsert each one with the other form fields, and then cancel the new connection screen, going back to the list.
- **Pre-requisite:** "Import all" should only be visible/triggerable when access, secret, endpoint and region are filled in.
