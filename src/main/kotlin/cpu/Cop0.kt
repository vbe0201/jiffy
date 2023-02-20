package io.github.vbe0201.jiffy.cpu

/**
 * The index of the [Status] register.
 */
const val STATUS_REGISTER = 12U

/*
 * The index of the [Cause] register.
 */
const val CAUSE_REGISTER = 13U

/**
 * Representation of the Status Register in CP0.
 */
@JvmInline
value class Status(val raw: UInt)

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
    @get:JvmName("getStatus")
    @set:JvmName("setStatus")
    var status = Status(0U)

    /**
     * Coprocessor Register 13: CAUSE register.
     */
    @get:JvmName("getCause")
    @set:JvmName("setCause")
    var cause = Cause(0U)

    /**
     * Sets the register with the given index to a value.
     */
    fun setRegister(index: UInt, value: UInt) {
        when (index) {
            STATUS_REGISTER -> this.status = Status(value)
            CAUSE_REGISTER -> this.cause = Cause(value)

            else -> println("[COP0] setRegister($index, 0x${value.toString(16)})")
        }
    }
}
