package io.github.vbe0201.jiffy.utils

/** Converts a [Boolean] to the equivalent [Int] value. */
inline fun Boolean.toInt() = if (this) 1 else 0

/** Converts a [Boolean] to the equivalent [UInt] value. */
inline fun Boolean.toUInt() = this.toInt().toUInt()

/**
 * Sign-extends a [UShort] value to [UInt].
 *
 * This preserves the sign bit of the original value.
 */
inline fun UShort.signExtend32(): UInt {
    // Casting from Short to UInt preserves the sign.
    return this.toShort().toUInt()
}

/**
 * Zero-extends a [UShort] value to [UInt].
 *
 * This just pads remaining bits with zeroes.
 */
inline fun UShort.zeroExtend32(): UInt {
    // Casting to UInt just fills the remaining bits with zeroes.
    return this.toUInt()
}
