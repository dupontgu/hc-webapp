@file:JsModule("wavefile-reader")
@file:JsNonModule

package dependencies

import org.khronos.webgl.Uint8Array

external class WaveFileReader {
    fun fromBuffer(buffer: Uint8Array)
    val data: WaveFileData
}

external class WaveFileData {
    val samples: Uint8Array
}