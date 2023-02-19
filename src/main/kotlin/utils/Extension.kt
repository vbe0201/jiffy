package io.github.vbe0201.jiffy.utils

/**
 * Sign-extends a [UByte] value to [UInt].
 *
 * This preserves the sign bit of the original value.
 */
fun UByte.signExtend32(): UInt {
    // Casting from Byte to Int preserves the sign.
    // Then bit casts the resulting value back to UInt.
    return this.toByte().toInt().toUInt()
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
    // Casting from Short to Int preserves the sign.
    // Then bit casts the resulting value back to UInt.
    return this.toShort().toInt().toUInt()
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
