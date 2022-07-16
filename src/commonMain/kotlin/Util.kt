fun ByteArray.mapInPlace(transform: (Byte) -> Byte) {
    for (i in this.indices) {
        this[i] = transform(this[i])
    }
}