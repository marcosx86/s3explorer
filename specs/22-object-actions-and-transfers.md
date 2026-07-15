# Object Actions and Transfers Spec

This specification dictates the implementation of contextual object actions and the new active transfers drawer.

## 1. Object Context Actions (Three-Dots Menu)
The three-dots button on list items (and inside the media viewer) will open a contextual drawer. The drawer header will display the object name, left-aligned.

### For Files:
- **Open with**: Downloads the file to a temporary cache folder and invokes the system "open with" Intent.
- **Share**: Generates a pre-signed URL and copies it to the device clipboard.
- **Preview**: Opens the app's media viewer for the selected file.
- **Rename**: Opens a modal with a pre-filled input field and a "Save" button to rename the object.
- **Download**: Opens the system folder selector (SAF) to save the file locally.
- **Delete**: Prompts a standard confirmation dialog before deletion.
- **Properties**: Opens a modal displaying detailed object metadata (Size, Hash, Content-Type, etc.).

### For Folders:
- **Share**: Generates a pre-signed URL and copies it to the clipboard.
- **Rename**: Opens a modal to rename the folder prefix.
- **Download**: Opens the system folder selector to save a ZIP archive of the folder's contents.
- **Delete**: Prompts a two-step confirmation dialog ("Are you sure?" double confirmation) due to recursive deletion risk.
- **Folder statistics**: Calculates total files and total size recursively, displaying the result in a bottom Toast with a timed slide-out animation.

## 2. Object Viewer Actions
- Add a three-dots menu to the top-right corner of the Object / Media Viewer screen.
- This menu spawns the exact same contextual actions as the file list drawer.

## 3. Transfers Drawer
The global progress bar at the bottom of the screen is replaced by an interactive Transfers Drawer.
- **Access**: Accessible via the connection drawer (top menu icon) and slides in from the bottom. Slides out automatically when there are no active transfers or upon user dismissal.
- **Header**: Displays overall progress totalization.
  ```text
  X.XX MB of X.XX MB  |  (X %)
  X.XX MB/s (dynamically updates every second)
  ```
  *(Volume units should format dynamically between GB, MB, KB, B)*
- **Transfer Items**: 
  Displayed as borderless rows for a clean aesthetic.
  - **Left Block**:
    ```text
    FILENAME (word wrap for full view)
    [PROGRESS BAR] (horizontal)
    XX.XX MB of XX.XX MB (XX %) (dynamically updates every second)
    ```
  - **Right Block**:
    - **Pause/Resume Button**
    - **Cancel Button**
- **Capabilities**: Every individual transfer must support pause, resume, and cancellation. Status indicators must reflect Success, Error, or Progress.
