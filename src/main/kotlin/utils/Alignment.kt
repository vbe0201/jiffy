package io.github.vbe0201.jiffy.utils

/** Aligns the value down to the next multiple of [align]. */
inline fun UInt.alignDown(align: UInt) = this and (align - 1U).inv()

/** Aligns the value up to the next multiple of [align]. */
inline fun UInt.alignUp(align: UInt) = (this + align - 1U).alignDown(align)

/** Checks if the value is aligned to a multiple of [align]. */
inline fun UInt.isAligned(align: UInt) = (this and (align - 1U)) == 0U
