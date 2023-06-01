package dependencies

import kotlin.js.Promise

@JsModule("@ffmpeg.wasm/main")
@JsNonModule
external object FFmpegRoot {
    fun createFFmpeg(options: dynamic): FFmpeg
}

external class FFmpeg {
    fun load(): Promise<Unit>
    fun run(vararg args: String): Promise<Unit>

    fun FS(vararg args: Any): Any
}