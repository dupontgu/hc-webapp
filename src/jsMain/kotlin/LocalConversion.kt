import dependencies.FFmpeg
import dependencies.FFmpegRoot
import dependencies.FileSaver
import dependencies.WaveFileReader
import kotlinx.coroutines.await
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get
import org.khronos.webgl.set
import org.w3c.files.Blob
import kotlin.js.Promise
import kotlin.math.absoluteValue

object BrowserFileConverter : FileConverter<Blob> {
    private val ffmpeg: FFmpeg

    init {
        val options = js("({'log':'true'})")
        ffmpeg = FFmpegRoot.createFFmpeg(options)
    }

    override suspend fun initialize() {
        ffmpeg.load().await()
    }

    override suspend fun convert(fileUpload: FileUpload<Blob>): ConversionResult = runCatching {
        val arrayBuffer = (fileUpload.data.asDynamic().arrayBuffer() as Promise<ArrayBuffer>).await()
        val dataArray = Uint8Array(arrayBuffer)
        val tempOutputFileName = "${fileUpload.name.hashCode().absoluteValue}.wav"
        with(ffmpeg) {
            // copy data to wasm file system
            val stream = FS("open", fileUpload.name, "w+")
            FS("write", stream, dataArray, 0, dataArray.length, 0);
            FS("close", stream)
            // run ffmpeg conversion on file to get to common format, in wasm file system
            run(
                "-i", fileUpload.name,
                "-acodec", "pcm_u8",
                "-ar", "24000",
                "-ac", "1",
                "-filter:a", "volume=1",
                tempOutputFileName
            ).await()

            // read the file back to process it
            val output = runCatching {
                FS("readFile", tempOutputFileName) as Uint8Array
            }.getOrElse {
                return@runCatching ConversionResult.UnknownFailure("Unable to convert file. If it was a valid audio file, please contact us to resolve the issue.")
            }

            // wrap data as wav file so we can extract samples only, no meta-data
            val waveData = WaveFileReader().apply { fromBuffer(output) }
            val samples = waveData.data.samples

            // process data
            for (i in 0 until samples.length) { samples[i] = samples[i].formatHcSample() }

            // save to user's file system as .htclp file
            FileSaver.saveAs(Blob(arrayOf(samples)), "${fileUpload.name}.$HTCLP_SUFFIX")
        }
        ConversionResult.Success
    }.getOrElse { ConversionResult.UnknownFailure(it.message ?: "Unknown Error") }
}
