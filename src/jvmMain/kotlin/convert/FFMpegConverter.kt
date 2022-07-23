package convert

import com.github.kokorin.jaffree.ffmpeg.FFmpeg
import com.github.kokorin.jaffree.ffmpeg.PipeInput
import com.github.kokorin.jaffree.ffmpeg.UrlOutput
import mapInPlace
import java.io.File
import java.io.InputStream
import java.nio.file.Path
import javax.sound.sampled.AudioSystem
import kotlin.experimental.and
import kotlin.io.path.deleteIfExists
import kotlin.io.path.inputStream

class FFMpegConverter(
    private val ffmpegBinaryPath: Path
) {
    fun convertToHcFile(
        inputStream: InputStream,
        filename: String,
        frequency: Int = 24000,
        timeLimitSeconds: Int? = null,
        fadeOutDurationSeconds: Int? = null,
    ): Result<File> = kotlin.runCatching {
        val interFile = kotlin.io.path.createTempFile(suffix = ".wav")
        val outputFile = kotlin.io.path.createTempFile(prefix = filename).toFile()
        FFmpeg.atPath(ffmpegBinaryPath)
            .addInput(PipeInput.pumpFrom(inputStream))
            .addOutput(UrlOutput.toPath(interFile))
            // 8 bit wav file
            .addArguments("-acodec", "pcm_u8")
            .addArguments("-ar", frequency.toString())
            // mono
            .addArguments("-ac", "1")
            .addArguments("-filter:a", "volume=1")
            .let {
                if (timeLimitSeconds == null) return@let it
                val res = it.addArguments("-t", timeLimitSeconds.toString())
                if (fadeOutDurationSeconds != null) {
                    res.addArguments(
                        "-af",
                        "afade=t=out:st=${timeLimitSeconds - fadeOutDurationSeconds}:d=${fadeOutDurationSeconds}"
                    )
                } else {
                    res
                }
            }
            .setOverwriteOutput(true)
            .execute()

        val audioStream = AudioSystem.getAudioInputStream(interFile.inputStream().buffered())
        val buffer = ByteArray(1024 * 128)

        // copy each sample from the wav file, format it, write to outputFile
        audioStream.use { input ->
            outputFile.outputStream().use { output ->
                var read = input.read(buffer)
                while (read >= 0) {
                    buffer.mapInPlace { it.formatHcSample() }
                    output.write(buffer, 0, read)
                    read = input.read(buffer)
                }
            }
        }
        println("output: ${outputFile.length()}")
        interFile.deleteIfExists()
        return@runCatching outputFile
    }
}

/**
 * Formats samples so that they are easier to be output directly from the HC firmware.
 * Effectively, the highest order bit determines which PWM output the sample will be emitted from
 * Remaining 7 bits indicate "magnitude" of a sample. If highest order bit is not set, magnitude is inverted.
 * Not exactly sure why, but this is what the HC Players are expecting.
 */
fun Byte.formatHcSample(): Byte {
    if (this.toInt().shr(7) != 0) return this
    return (127 - (this and 127)).toByte()
}