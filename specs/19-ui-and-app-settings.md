# UI and Application Settings Spec

This specification outlines the refinements for the general UI and the expansion of the Settings screens.

## 1. General UI Refinements
- **Dark Mode**: Implement a three-way combo for the theme: Light, Dark, and System Default.
- **Secure Application**: Enforce an app lock on application launch or reactivation if enabled by the user in settings.
- **Splashscreen**: Introduce an artificial delay of 1500ms to the splashscreen to ensure smooth transition and branding visibility.
- **About Screen**: Create a dedicated "About" screen (replacing any modal) featuring:
  - App logo at the top with "Lorem ipsum" description.
  - Donation section featuring a clickable PIX QR code (tapping copies the PIX key to the clipboard).
  - Developer's GitHub profile link.
  - App version displayed centered at the bottom.

## 2. Global Settings Expansion
Available from the connection drawer, named "Global settings" to avoid confusion with isolated account settings.
- **System**: Add "Trust insecure certificates" toggle to skip SSL verification (default: false).
- **Lock**: Add "Enable native lock screen" to secure the app using device methods (Biometrics/PIN/Pattern).
- **View**: 
  - Add "Choose theme" (Light, Dark, System).
  - Add explicit toggles for "Show image thumbnails" and "Show video thumbnails".
- **Miscellaneous**: Add "Custom user agent" configuration via a modal input (default: `S3Explorer/1.0 (Android)`).

## 3. Account Settings Expansion
Settings isolated per connection profile, featuring titles and smaller description text.
- **Privacy**: 
  - E2E encryption toggle (encrypt on upload, decrypt on download). Requires a passphrase stored in Room.
- **Bucket**: 
  - MD5 verification (calculate hash and send `Content-MD5` header).
  - Storage Class input (modal with string input to support S3-compatible classes).
- **Upload**: 
  - Skip same file upload (size & modified time match, ignores files < 10KB).
  - Upload transfers (concurrent limits: 1 to 10, default 2).
  - Multipart upload config: Concurrent parts (1-15, default 5), Chunk size (5-100MB, default 10MB), Start threshold (10-510MB, default 150MB).
- **Thumbnails**: 
  - Generate thumbnails locally (toggle).
  - Upload thumbnails remotely (toggle - disabled if generation is disabled).
- **Cleanup**: 
  - Clear document cache.
  - Clear local thumbnail cache.
  - Delete pending multipart uploads.
- **Network**: 
  - Upload timeout (modal to set ms, default 300000).
  - Download timeout (modal to set ms, default 300000).
