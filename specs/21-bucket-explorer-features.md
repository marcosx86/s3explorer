# Bucket Explorer Features Spec

This specification focuses on enhancements to the main bucket file explorer interface.

## 1. Floating Action Drawer
- Replace the standalone FAB action with a drawer that slides from the bottom when the Plus button is tapped.
- **Options**:
  - `New folder`: Opens a modal with a text input field to name the folder.
  - `Upload file`: Invokes the system file picker (`ActivityResultContracts.GetContent`).
  - `Take photo`: Invokes the system camera to capture an image and immediately upload it.

## 2. Selection Mode
- When the toolbar is toggled into "Selection mode", selection checkboxes must appear on the left side of every list item (files and folders).

## 3. Search and Filtering
- Add a Search button to the toolbar.
- Tapping the search button reveals a new `Row` containing a text input field.
- The input will dynamically filter the currently visible items (lists or objects) based on name matching.

## 4. Sorting Enhancements
- Expand the sorting dropdown block to include a "Folders first" toggle, placed right below the "Show hidden" option.
- **Behavior**: If true, all folders are grouped and pinned to the top of the list during sort operations. If false, folders and files are mixed based solely on the sorting field (e.g., Alphabetical).
