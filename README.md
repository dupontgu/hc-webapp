# HotClasps Web App

## [Live Version](https://www.hotclasps.com)
Let me know if it's dead?

## Features
- Minimal info about the project, temporary link to interest form.
- File conversion: [convert](https://www.hotclasps.com/upload) any audio file into a ".htclp" file that is playable on the open source cartridges.
- Artwork formatting: [upload](https://www.hotclasps.com/artwork) an image and crop/resize it to fit in the case.

## Implementation
- Kotlin Multiplatform!!
  - Frontend uses [Compose for web](https://compose-web.ui.pages.jetbrains.team/). Sources in [jsMain](src/jsMain).
  - Backend uses [Ktor](https://ktor.io/). Sources in [jvmMain](src/jvmMain). It's configured to run on Google Apps Engine, but isn't tightly coupled. The backend is currently only doing some minor routing.
- Audio conversion uses [FFmpeg](https://ffmpeg.org/) as a preliminary step. Runs via WASM in the browser.