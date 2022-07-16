package wrapper

object FFMpegWrapper {
    // Being lazy here and only checking for my development machine. Change this if you're working on something
    // other than a mac. You may have to load an appropriate binary into resources/dev/{os} as well.
    // binaries here: https://ffbinaries.com/downloads
    val ffmpegPath = if (System.getProperty("os.name").lowercase().contains("mac")) {
        "dev/macos"
    } else {
        "binaries"
    }
}