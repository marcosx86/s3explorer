# Connection Management Refinements Spec

This specification outlines the refinements to the connection management flows.

## 1. Connections List Updates
- **Bucket Listing Modal**: Move the "List buckets" feature from the drawer to a dedicated modal.
- **Combo-box Integration**: Display the listed buckets in a combo-box for easy selection.
- **Bulk Import**: Provide an "Import all" button that multiplies the connection items immediately (one for each bucket), along with a "Cancel" action.

## 2. New Connection Screen Updates
- **Advanced Options**: Wrap "Endpoint URL" and "Skip insecure connection" inside a collapsible "Advanced" section to declutter the default view.
- **Field Reordering**: Adjust the form layout so the fields appear in the following order:
  1. Access Key
  2. Secret Key
  3. Region
- **Validation**: Add a "Validate" button to test the connection parameters against the S3 endpoint before saving the profile.
