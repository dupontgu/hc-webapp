import kotlin.experimental.and

fun ByteArray.mapInPlace(transform: (Byte) -> Byte) {
    for (i in this.indices) {
        this[i] = transform(this[i])
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