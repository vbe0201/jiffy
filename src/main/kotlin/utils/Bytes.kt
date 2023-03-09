package io.github.vbe0201.jiffy.utils

import java.nio.ByteBuffer

/** Creates a new [ByteBuffer] over an array of [size] byte [value]s. */
inline fun makeArrayBuffer(size: UInt, value: Byte): ByteBuffer {
    val buffer = ByteArray(size.toInt()) { value }
    return ByteBuffer.wrap(buffer)
}

/** Gets the total number of bytes currently in the buffer. */
inline fun ByteBuffer.size(): UInt {
    return this.limit().toUInt()
}

/** Reads an [UByte] value at the given [index]. */
inline fun ByteBuffer.uget(index: Int): UByte {
    return this.get(index).toUByte()
}

/** Reads a [UShort] value at the given [index]. */
inline fun ByteBuffer.getUShort(index: Int): UShort {
    return this.getShort(index).toUShort()
}

/** Reads a [UInt] value at the given [index]. */
inline fun ByteBuffer.getUInt(index: Int): UInt {
    return this.getInt(index).toUInt()
}

/** Writes a [UByte] value at the given [index]. */
inline fun ByteBuffer.uput(index: Int, value: UByte) {
    this.put(index, value.toByte())
}

/** Writes a [UShort] value at the given [index]. */
inline fun ByteBuffer.putUShort(index: Int, value: UShort) {
    this.putShort(index, value.toShort())
}

/** Writes a [UInt] value at the given [index]. */
inline fun ByteBuffer.putUInt(index: Int, value: UInt) {
    this.putInt(index, value.toInt())
}
