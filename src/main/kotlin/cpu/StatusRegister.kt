package io.github.vbe0201.jiffy.cpu

/** Representation of the Status Register in COP0. */
@JvmInline
value class StatusRegister(val raw: UInt) {
    /** Whether the Data Cache is isolated in loads/stores. */
    inline fun de(): Boolean = (this.raw and 0x00010000U) != 0U

    /** Indicates if Boot Exception Vectors are located in RAM/ROM. */
    inline fun bev(): Boolean = (this.raw and 0x00400000U) != 0U

    /** Returns a new status register value upon entering an exception. */
    inline fun enterException(): StatusRegister {
        // Bits [5:0] store 3 pairs of Interrupt Enable/User Mode bits
        // that behave like a stack. Entering an exception pushes a
        // pair of zeroes to disable interrupts and put the CPU in
        // Kernel Mode. One entry gets discarded since the kernel is
        // responsible for handling more than two levels of recursion.
        val mode = this.raw.shl(2).and(0x3FU)
        return StatusRegister(this.raw.and(0x3FU.inv()).or(mode))
    }

    /** Returns a new status register value upon leaving from exception. */
    inline fun leaveException(): StatusRegister {
        val mode = this.raw.and(0x3FU).shr(2)
        return StatusRegister(this.raw.and(0x3FU.inv()).or(mode))
    }
}
