package io.github.vbe0201.jiffy.cpu

import io.github.vbe0201.jiffy.utils.toUInt

/**
 * The index of the [Status] register.
 */
const val STATUS_REGISTER = 12U

/*
 * The index of the [Cause] register.
 */
const val CAUSE_REGISTER = 13U

/**
 * The index of the EPC register.
 */
const val EPC_REGISTER = 14U

/**
 * Representation of the Status Register in CP0.
 */
@JvmInline
value class Status(val raw: UInt) {
    fun ie(): Boolean = (this.raw and 0x00000001U) != 0U

    fun exl(): Boolean = (this.raw and 0x00000002U) != 0U

    fun erl(): Boolean = (this.raw and 0x00000004U) != 0U

    fun s(): Boolean = (this.raw and 0x00000008U) != 0U

    fun u(): Boolean = (this.raw and 0x00000010U) != 0U

    fun ux(): Boolean = (this.raw and 0x00000020U) != 0U

    fun sx(): Boolean = (this.raw and 0x00000040U) != 0U

    fun kx(): Boolean = (this.raw and 0x00000080U) != 0U

    fun im(): UInt = (this.raw and 0x0000FF00U) shr 8

    fun de(): Boolean = (this.raw and 0x00010000U) != 0U

    fun ce(): Boolean = (this.raw and 0x00020000U) != 0U

    fun ch(): Boolean = (this.raw and 0x00040000U) != 0U

    fun nmi(): Boolean = (this.raw and 0x00080000U) != 0U

    fun sr(): Boolean = (this.raw and 0x00100000U) != 0U

    fun ts(): Boolean = (this.raw and 0x00200000U) != 0U

    fun bev(): Boolean = (this.raw and 0x00400000U) != 0U

    fun re(): Boolean = (this.raw and 0x02000000U) != 0U

    fun fr(): Boolean = (this.raw and 0x04000000U) != 0U

    fun rp(): Boolean = (this.raw and 0x08000000U) != 0U

    fun cu0(): Boolean = (this.raw and 0x10000000U) != 0U

    fun cu1(): Boolean = (this.raw and 0x20000000U) != 0U

    fun cu2(): Boolean = (this.raw and 0x40000000U) != 0U

    fun xx(): Boolean = (this.raw and 0x80000000U) != 0U
}

/**
 * Representation of the CAUSE Register in CP0.
 */
@JvmInline
value class Cause(val raw: UInt)

/**
 * Implementation of the System Control Processor (CP0).
 *
 * This unit is responsible for handling cache configuration and
 * CPU exceptions and is mandated by the MIPS architecture.
 */
class Cop0 {
    /**
     * Coprocessor Register 12: Status register.
     */
    var status = Status(0U)

    /**
     * Coprocessor Register 13: CAUSE register.
     */
    var cause = Cause(0U)

    /**
     * Coprocessor Register 14: Exceptional PC register.
     */
    var epc = 0U

    /**
     * Gets the value of a register with the given index.
     */
    fun getRegister(index: UInt): UInt {
        return when (index) {
            STATUS_REGISTER -> this.status.raw
            CAUSE_REGISTER -> this.cause.raw
            EPC_REGISTER -> this.epc

            else -> 0U
        }
    }

    /**
     * Sets the register with the given index to a value.
     */
    fun setRegister(index: UInt, value: UInt) {
        when (index) {
            STATUS_REGISTER -> this.status = Status(value)
            CAUSE_REGISTER -> this.cause = Cause(value)
            EPC_REGISTER -> this.epc

            else -> println("[COP0] setRegister($index, 0x${value.toString(16)})")
        }
    }

    /**
     * Puts the COP0 state into exception mode.
     */
    fun raiseException(epc: UInt, delayed: Boolean, kind: ExceptionKind) {
        // Bits [5:0] of the Status Register store three pairs of
        // Interrupt Enable/User Mode bits that behave like a stack.
        // Entering an exception pushes a pair of zeroes to disable
        // interrupts and put the CPU in Kernel mode. The original
        // third entry gets discarded since the kernel is responsible
        // for handling more than two recursive exception levels.
        val mode = ((this.status.raw and 0x3FU) shl 2) and 0x3FU
        this.status = Status((this.status.raw and 0x3FU.inv()) or mode)

        // Update the cause of the exception.
        this.cause = Cause((delayed.toUInt() shl 31) or (kind.type shl 2))

        // Save the faulting program counter.
        this.epc = epc
    }

    /**
     * Restores internal state when leaving exceptional state.
     */
    fun restoreAfterException() {
        val mode = (this.status.raw and 0x3FU) shr 2
        this.status = Status((this.status.raw and 0x3FU.inv()) or mode)
    }
}
