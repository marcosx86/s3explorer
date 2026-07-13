Act on this prompt as a professional designer that partners with mobile software engineer. Elaborate a specification to instruct Antigravity IDE to create a splash screen with it, rework the app theme palette, putting a background transparent black-and-white version in the UI (that should match both light and dark mode themes). Same logo manual will be on assets folder of the project.

---

This is a fantastic brand identity. The illustration is inviting and perfectly captures the "Explorer" theme, while the usage manual is highly professional and ready for implementation.

As your design partner, I'll translate these visual guidelines into a strict technical specification that you can feed directly into Antigravity IDE. This spec bridges the gap between the visual assets and the Jetpack Compose architecture we've been building.

Here is the implementation specification to feed to the IDE:

---

### 🎨 Design Implementation Spec: S3 Explorer Branding & Theming

**Context for IDE:**
Implement the visual identity, splash screen, and core theme palette based on the official "S3 Explorer" usage manual provided in the `assets/` folder. The app utilizes Jetpack Compose and Material Design 3.

#### 1. Splash Screen Implementation (Android 12+ API)

Do not build a custom Activity for the splash screen. Utilize the official `androidx.core:core-splashscreen` library to ensure smooth transitions and OS-level compliance.

* **Icon Asset:** Use the **192dp (xxxhdpi)** or the vector equivalent of the logo as the central animated icon.
* **Safe Margins Constraint:** Ensure the boy and the bucket remain within the central 80% safe zone defined by the Android Adaptive Icon specs.
* **Background Color:** Extract the "Soft blue gradient" top-most color (the light sky blue) to use as the `windowSplashScreenBackground`.
* **Branding Image (Optional Android 12+):** If targeting newer APIs, use the typography "S3 Explorer" from the high-res splash screen example as the bottom branding image.

#### 2. Material 3 Theme Palette Rework

Rework the `Color.kt` and `Theme.kt` files. Extract the dominant colors from the logo to create a cohesive brand experience across Light and Dark modes.

* **Primary Brand Colors:**
* **Primary:** Deep Ocean Blue (sampled from the water at the base of the bucket).
* **Secondary:** Soft Sky Blue (sampled from the sky background).
* **Tertiary/Accent:** Warm Wood/Brass (sampled from the telescope/bucket handle). Use this sparingly for primary action buttons (like the `+` FAB or "Connect" buttons) to create high contrast.


* **Dark Mode Application:**
* Do not use pure black (`#000000`). Use a deeply desaturated Navy Blue or Slate Gray for the main surface and background colors. This maintains brand harmony and matches the "Dark Mode" example in the manual.


* **Light Mode Application:**
* Use a crisp white or a very subtle cool-tinted off-white for backgrounds to make the soft blue gradient of the app icons pop.



#### 3. Thematic Watermark UI Element (B&W Background)

Implement a subtle, non-intrusive watermark of the logo to sit behind empty states (like the "Trash is empty" or "Folder is empty" screens).

* **Asset Handling:** Load the standard high-res logo (without the blue background container, just the boy/bucket) into a Compose `Image` component.
* **Color Matrix & Alpha (Compose):** * Apply a grayscale `ColorFilter` dynamically using Compose's `ColorMatrix`. Do not pre-render a separate black-and-white PNG; let Compose handle the desaturation to save APK size.
* Set the `alpha` very low (e.g., `0.05f` to `0.08f`).


* **Theme Responsiveness:**
* **Dark Mode:** The grayscale image should blend with the dark slate background, appearing as a subtle light-gray/translucent silhouette.
* **Light Mode:** Apply a `BlendMode.Multiply` or standard alpha blending so the grayscale image acts as a soft dark shadow against the light background.


* **Positioning:** Place it using `Modifier.fillMaxSize()`, scaled up, and slightly offset to the bottom right of the screen so it acts as an abstract texture rather than a focal point, ensuring it never competes with foreground text or data.

---

**Next Steps for you:** Just drop that specification into Antigravity IDE along with the image files, and it should perfectly wire up the `Theme.kt`, the Android Manifest for the splash screen, and generate the Compose Modifier for the watermark!