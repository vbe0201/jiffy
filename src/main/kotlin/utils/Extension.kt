package io.github.vbe0201.jiffy.utils

/**
 * Sign-extends a [UByte] value to [UInt].
 *
 * This preserves the sign bit of the original value.
 */
fun UByte.signExtend32(): UInt {
    // Casting from Byte to UInt preserves the sign.
    return this.toByte().toUInt()
}

/**
 * Zero-extends a [UByte] value to [UInt].
 *
 * This just pads remaining bits with zeroes.
 */
fun UByte.zeroExtend32(): UInt {
    // Casting to UInt just fills the remaining bits with zeroes.
    return this.toUInt()
}

/**
 * Sign-extends a [UShort] value to [UInt].
 *
 * This preserves the sign bit of the original value.
 */
fun UShort.signExtend32(): UInt {
    // Casting from Short to UInt preserves the sign.
    return this.toShort().toUInt()
}

/**
 * Zero-extends a [UShort] value to [UInt].
 *
 * This just pads remaining bits with zeroes.
 */
fun UShort.zeroExtend32(): UInt {
    // Casting to UInt just fills the remaining bits with zeroes.
    return this.toUInt()
}
