package io.github.vbe0201.jiffy.cpu

import io.github.vbe0201.jiffy.utils.toUInt

/** Representation of the CAUSE Register in CP0. */
@JvmInline
value class CauseRegister(val raw: UInt) {
    /** Creates a new cause register value encoding an exception. */
    inline fun exception(delayed: Boolean, kind: ExceptionKind): CauseRegister {
        return CauseRegister((delayed.toUInt() shl 31) or (kind.raw shl 2))
    }
}
