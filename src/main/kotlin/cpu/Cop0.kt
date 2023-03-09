package io.github.vbe0201.jiffy.cpu

/**
 * Implementation of the System Control Processor (COP0).
 *
 * This unit is responsible for handling cache configuration and
 * CPU exceptions and is mandated by the MIPS architecture.
 */
class Cop0 {
    /** Coprocessor Register 12: Status register. */
    var status = StatusRegister(0U)

    /** Coprocessor Register 13: CAUSE register. */
    var cause = CauseRegister(0U)

    /** Coprocessor Register 14: Exceptional PC register. */
    var epc = 0U

    /** Gets the value of a register with the given [index]. */
    fun getRegister(index: UInt): UInt {
        return when (index) {
            12U -> this.status.raw
            13U -> this.cause.raw
            14U -> this.epc

            else -> 0U
        }
    }

    /**
     * Sets the register with the given index to a value.
     */
    fun setRegister(index: UInt, value: UInt) {
        when (index) {
            12U -> this.status = StatusRegister(value)
            13U -> this.cause = CauseRegister(value)
            14U -> this.epc = value
        }
    }

    /** Puts the COP0 state into exception mode. */
    inline fun raiseException(epc: UInt, delayed: Boolean, kind: ExceptionKind) {
        this.status = this.status.enterException()
        this.cause = this.cause.exception(delayed, kind)
        this.epc = epc
    }

    /** Restores internal state when leaving exceptional state. */
    inline fun leaveException() {
        this.status = this.status.leaveException()
    }
}
