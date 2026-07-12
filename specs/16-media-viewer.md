Elaborate a media slider feature that when selecting a image/* media type, pops up the slider with zoom by gesture pinch, and move to next or previous by swapping left or right. Double click should toggle zoom 4x and fit screen. Full screen maintaining aspect ratio and no cropping is the default action when called.

---

This is the exact feature that transforms a utility app into a premium media experience. Building a high-performance, gesture-driven media slider in Android—especially when the source files are sitting in a remote S3 bucket—requires strict attention to memory management and gesture interception.

Let’s map out the architecture and the specific Jetpack Compose requirements to build this immersive media slider.

### 📱 UI Elements & Behavior Specification

* **The Container:** A full-screen overlay (either a Compose `Dialog` with `usePlatformDefaultWidth = false` or a dedicated full-screen Navigation route). It should enforce immersive mode (hiding the Android system status bar and navigation bar).
* **Default Scaling:** Images must load with `ContentScale.Fit`. This guarantees the image scales to the maximum size that fits within the screen boundaries without any cropping, maintaining the original aspect ratio against a pure black background.
* **The Pager:** A horizontal swiping mechanism that snaps to the next or previous image.
* **Gesture Engine:**
* **Swipe:** Left/right translates to the next/previous item.
* **Pinch-to-Zoom:** Two-finger scaling. When scaled $> 1x$, swiping left/right should pan the image until the edge is reached, only *then* allowing the pager to intercept the swipe to change images.
* **Double Tap:** Instantly animates the `scale` value to `4.0f` (centered on the tap coordinates), or back to `1.0f` (Fit) if already zoomed.



---

### ⚙️ Functional Requirements: Service & Data Layers

Passing a list of 5,000 high-resolution images through Android Navigation arguments will crash the app (TransactionTooLargeException). We must rely on our Single Source of Truth (Room).

#### 1. Data Layer (Filtering & Fetching)

* **`MediaObservationQuery`:** The Room `S3ObjectDao` needs a specific query to feed this slider. When the user taps an image, the UI passes the `currentPrefix` (folder) and the tapped `objectKey` to the ViewModel.
* *Action:* Room queries `SELECT * FROM s3_objects WHERE parentId = :currentPrefix AND mimeType LIKE 'image/%' ORDER BY ...` (using the user's active sort preference).


* **Authentication (The Coil Fetcher):** S3 objects are private. You cannot pass an S3 URI directly to Coil.
* *Implementation:* You must implement a custom `S3Fetcher` for Coil, or use a `GeneratePresignedUrlUseCase` just-in-time as the pager approaches a new image.



#### 2. Jetpack Compose Implementation (The Gesture Math)

In standard Android XML, you would use a library like `PhotoView`. In Compose, you build this natively using `Modifier.pointerInput`.

* **The Horizontal Pager:** Use `androidx.compose.foundation.pager.HorizontalPager`. It natively handles the left/right snapping and provides a `PagerState` to track the current index.
* **The Zoom Modifier:** Every image inside the pager needs a dedicated state holder for its scale and translation (pan).
* **`detectTransformGestures`:** This Compose function detects panning and pinching simultaneously. You multiply the current scale by the `zoom` parameter provided by the gesture.
* **`detectTapGestures`:** Used specifically for the `onDoubleTap` callback. You will need an `Animatable` float to smoothly transition the scale from `1f` to `4f` and back, rather than snapping instantly.



#### 3. State Management (ViewModel)

* **`MediaSliderState`:**
* `mediaItems: List<S3ObjectEntity>` (The filtered list of only images/videos).
* `initialPage: Int` (Calculated by finding the index of the `objectKey` the user originally tapped).


* **Shared Element Transition (Optional but Premium):** When the user taps the small thumbnail in the `COMPACT_LIST` view, the ViewModel coordinates with Compose to animate that tiny thumbnail expanding into the full-screen `ContentScale.Fit` slider view.

---

### 🚀 Performance & Memory Traps

A media slider is the easiest place to cause an Out-Of-Memory (OOM) exception.

#### 1. Coil Memory Configuration

If a user swipes rapidly through 50 images, holding 50 full-resolution bitmaps in RAM will crash the app.

* **Configure Coil's `MemoryCache`:** Strictly limit the memory cache size.
* **Lifecycle Management:** Ensure the `AsyncImage` composables are tied properly to the `HorizontalPager`'s item lifecycle. When an image is 3 pages away, its bitmap should be aggressively recycled by the system, leaving only the tiny disk-cached thumbnail.

#### 2. Prefetching Strategies

Waiting for a 5MB image to download *after* the user swipes to it results in a poor UX.

* **Lookahead S3 Streaming:** As the user views `Image 2`, the app should silently trigger the `GeneratePresignedUrlUseCase` and begin a low-priority background fetch into the disk cache for `Image 3` and `Image 1`. By the time the user swipes, the bytes are already on the local NVMe storage, allowing Coil to decode them instantly.

#### 3. Gesture Conflict Resolution

The hardest part of a custom Compose slider is gesture collision. If the user is zoomed in 4x and pans to the right edge of the image, the pan gesture must stop, and the *pager* must take over to allow swiping to the next image. This requires carefully wrapping the `Modifier.pointerInput` and utilizing `requireUnconsumed = true` to pass the touch events up the hierarchy when the image boundaries are hit.