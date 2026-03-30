# PageCurlEffect for Android

A high-performance Android application that demonstrates a realistic 3D page curl effect for PDF documents using OpenGL ES. This project allows users to select a PDF from their device and navigate through it with an interactive, visually appealing page-turning animation.

## Features

- **Realistic 3D Animation:** Smooth and interactive page curl effect powered by OpenGL ES.
- **PDF Integration:** Seamlessly renders PDF pages using Android's native `PdfRenderer`.
- **Dynamic Content:** Supports loading any PDF file from the device storage.
- **Edge-to-Edge Support:** Modern UI implementation with full-screen support.
- **Optimized Performance:** Uses custom GLSurfaceView and specialized shaders for efficient rendering.

## Tech Stack

- **Language:** Kotlin
- **Graphics:** OpenGL ES (via `GLSurfaceView`, `Renderer`, and custom `CurlMesh`)
- **PDF Handling:** `android.graphics.pdf.PdfRenderer`
- **Architecture:** Modern Android development practices (ViewBinding, Activity Result API).
- **UI:** XML Layouts with Material Design components.

## Getting Started

### Prerequisites

- Android Studio Flamingo or newer.
- Android SDK 24 (Android 7.0) or higher.

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/CurlPageEffect.git
   ```
2. Open the project in Android Studio.
3. Sync the project with Gradle files.
4. Run the app on an emulator or a physical device.

## Usage

1. Launch the application.
2. Tap on the **"Select PDF"** button.
3. Choose a PDF file from your device's file picker.
4. Use swipe gestures on the screen to flip through the pages with the curl effect.

## Project Structure

- `MainActivity.kt`: Handles PDF file picking and coordinates between the `PdfRenderer` and the OpenGL surface.
- `MyGLSurfaceView.kt`: Custom GL surface view that handles touch events for the page flip.
- `PageCurlRenderer.kt`: Manages the OpenGL rendering cycle and textures.
- `CurlMesh.kt`: Contains the logic for the 3D geometry and vertex calculations of the curling page.

## Contributing

Contributions are welcome! If you find any issues or have suggestions for improvements, feel free to open an issue or submit a pull request.

1. Fork the Project.
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`).
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`).
4. Push to the Branch (`git push origin feature/AmazingFeature`).
5. Open a Pull Request.

## License

Distributed under the MIT License. See `LICENSE` for more information.

---
Developed by [DevNight](https://github.com/devnight)
