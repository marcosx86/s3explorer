# Screens

- Dark mode (three-way combo: light/dark/system)
- Secure application on app launch or reactivation

## Splashscreen

- Add delay to splashscreen (1500ms)

## Connections list

- "List buckets" from drawer to modal, list buckets on combo-box, buttons to "import all" with immediate action multiplying connection items and "cancel"

## New connection

- Collapsible section "Advanced" for Endpoint URL, Skip insecure connection
- Reorder fields as: Access Key, Secret Key, Region
- Add "Validate" connection button

## Bucket contents

- Plus button opens a drawer from bottom with "New folder", "Upload file" and "Take photo" options, last one uses the system camera to capture picture and upload it immediately. New folder pops up a modal with input field for name. Upload file calls system file picker. 
- Selection boxes on left side of thumbail shows on activating toolbar "Selection mode"
- Search button should reveal search bar as new Row() with input + filtering items in list or objects
- Add toggable "Folders first" below "Show hidden" in Sort button - Group all folders together on top if true or mix them with files during sort operation

## Object actions

Drawer having the object name as title, left-aligned, to be implemented in the bucket listing's rows (three vertical dots button):

***For files***

- Open with (should download to temp folder then use system "open with" drawer)
- Share (generate pre-signed URL and puts on clipboard to paste)
- Preview (opens the media viewer for that file)
- Rename (opens a input inside a modal with current value and Save button)
- Download (opens system folder selector to save the file)
- Delete (with confirmation dialog)
- Properties (modal with object metadata)

***For folders***

- Share (generate pre-signed URL and puts on clipboard to paste)
- Rename (opens a input inside a modal with current value and Save button)
- Download (opens system folder selector to save a ZIP from the folder contents)
- Delete (with confirmation dialog, and ARE YOU SURE double confirmation)
- Folder statistics (calculates the total files and size, and toasts in the bottom of the screen with timed slide out)

## Object viewer

- Put three-dots-menu on top-right corner and spawn the same object actions

## Transfers drawer

This drawer replaces the global progress bar at the bottom of the screen. It should display all active and recent transfers, showing progress, speed, and status (success/error). Each transfer should be pausable, resumeble and cancellable. It slides in from the bottom of the screen and slides out when there are no active transfers or when the user closes it. It is accessible from the connection drawer (left border, top menu icon).

Drawer header shows totalization as:

```
X.XX MB of X.XX MB  |  (X %)
X.XX MB/s (dinamically updates every second)
```

Dynamic volume representations, from GB, MB, KB and B.

Items should be displayed as rows with no border for clean aspect, each row having Download/Upload as title, and two blocks being one in right with pause/resume and cancel buttons, and in the left block this fashion:

```
FILENAME (word wrap for full view)
[PROGRESS BAR] (horizontal)
XX.XX MB of XX.XX MB (XX %) (dinamically updates every second)
```

## Account Settings

Each option should have title and a description below it with smaller font

***"Privacy" section***

- E2E encription / Turn on to encrypt files when uploading, turn off to encrypt when downloading - Should ask for a passphrase to use it, must be stored in the Room database

***"Bucket" section***

- MD5 verification / Object hash calculation and Content-MD5 header on upload - TO BE REFINED LATER
- Storage Class - Opens a modal with input field + save/cancel buttons, accept arbitrary values to comply with S3-compatible stores (default is empty)

***"Upload" section***

- Skip same file upload / Skip file upload if size and modification time matches. Doesn't apply to files below 10KB
- Upload transfers / Number of concurrent file uploads. Applies for each upload separately - slider from 1 to 10, default is 2
- Multipart upload / Files over X MB will be uploaded in Y MB chunks using Z parallel workers for higher throughput, decrease to lower memory usage. Multipart is mandatory for files over 5GB. - Three sliders below defines X, Y and Z: Concurrent part uploads, from 1 to 15, default to 5; Chunk size, from 5 to 100 MB with steps of 5 MB, default to 10 MB; Start threshold, from 10 to 510 MB with steps of 20 MB, default is 150 MB.

***"Thumbnails" section***

- Generate thumbnails / Generates and stores them locally. Makes subequent preview faster at the initial processing cost. - We already do that, we will just parametrize, and if false, we show only the placeholders that we already have.
- Upload thumbnails / Store generated thumbnails remotely, so they don't need to be generated on your other devices. - We do not upload thumbnails if it's turned off, just keep at local Android cache storage.

***"Cleanup" section***

- Clear document cache / Clears the cache of downloaded objects from the bucket
- Clear thumbnails cache / Clears the cache of generated thumbnails locally only
- Delete pending multipart uploads / This deletes all pending multipart uploads

***"Network" section***

- Upload timeout / (below current value in ms) - Opens modal to set miliseconds, default is 300000. Sets the timeout for upload operations
- Download timeout / (below current value in ms) - Opens modal to set miliseconds, default is 300000. Sets the timeout for download operations 

## Global Settings

In the connection drawer we show "Settings" but in the active UI we call "Global settings" to avoid confusion with Account settings

***"System" section***

- Trust insecure certificates - Skip verification of SSL certificates, enables connection to S3-compatible stores with self-signed certificates (default is false)

***"Lock" section***

- Enable native lock screen / Uses configured methods (e.g. biometrics/pattern/PIN) from your device

***"View" section***

- Choose theme - Selectable options: Light, Dark and System
- Show image thumbnails - Additional check explict for image files on showing thumbnails
- Show video thumbnails - Additional check explict for video files on showing thumbnails

***"Miscellaneous" section***

- Custom user agent / (shows the current value below) - Opens modal to set the user agent, default is "S3Explorer/1.0 (Android)"

## About screen

Instead of a modal, we will open a separate screen to show a Lorem ipsum content with the logo on top. Below we will have a donation section, with clickable PIX QR code (copies the PIX key), developer's GitHub profile link, and the app version at the bottom, centered.