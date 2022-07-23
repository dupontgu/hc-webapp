# HotClasps Web App

## [Live Version](https://www.hotclasps.com)
Let me know if it's dead?

## Features
- Minimal info about the project, temporary link to interest form.
- File conversion: [convert](https://www.hotclasps.com/upload) any audio file into a ".htclp" file that is playable on the open source cartridges.

## Implementation
- Kotlin Multiplatform!!
  - Frontend uses [Compose for web](https://compose-web.ui.pages.jetbrains.team/). Sources in [jsMain](src/jsMain).
  - Backend uses [Ktor](https://ktor.io/). Sources in [jvmMain](src/jvmMain). It's configured to run on Google Apps Engine, but isn't tightly coupled.
- Audio conversion uses [FFmpeg](https://ffmpeg.org/) as a preliminary step. Note that you'll need to [embed a binary](src/jvmMain/kotlin/FFMpegWrapper.kt) if you want to run this locally. 