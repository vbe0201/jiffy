package io.github.vbe0201.jiffy.jit.state

import io.github.vbe0201.jiffy.cpu.BIOS_START
import io.github.vbe0201.jiffy.cpu.Bus
import io.github.vbe0201.jiffy.jit.decoder.INSTRUCTION_SIZE
import io.github.vbe0201.jiffy.jit.decoder.Instruction

/**
 * The execution context of the JIT.
 *
 * This class wraps raw execution state of the emulated MIPS processor,
 * which is accessed and manipulated by generated JIT code.
 */
class ExecutionContext(
    /**
     * The [Bus] interface to use during program execution.
     */
    val bus: Bus
) {
    /**
     * The execution [State] of this context.
     */
    var state = State.INITIAL

    /**
     * The general-purpose register for the CPU.
     */
    @get:JvmName("getGprs")
    var gprs = UIntArray(32) { 0U }
        private set

    /**
     * The special-purpose program counter register where the next
     * instruction should be fetched.
     *
     * Hard-wired to start at the first BIOS instruction.
     */
    @set:JvmName("setPc")
    var pc = BIOS_START

    /**
     * The special-purpose register which holds the high 32 bits of
     * multiplication result; remainder of division.
     */
    val hi = 0U

    /**
     * The special-purpose register which holds the low 32 bits of
     * multiplication result; quotient of division.
     */
    val lo = 0U

    /**
     * The coprocessor register 12: Status Register.
     */
    @get:JvmName("getCop0Status")
    @set:JvmName("setCop0Status")
    var status = 0U

    /**
     * Reads a 32-bit value from the given memory address through
     * the CPU bus.
     */
    @JvmName("read32")
    fun read32(addr: UInt): UInt {
        // TODO: Alignment check.
        return this.bus.read32(addr)
    }

    /**
     * Writes a 32-bit value to a given memory address thorugh the
     * CPU bus.
     */
    @JvmName("write32")
    fun write32(addr: UInt, value: UInt) {
        // TODO: Alignment check.
        this.bus.write32(addr, value)
    }

    /**
     * Throws a [NotImplementedError] when called.
     *
     * The main purpose of this stub is prototyping; it is expected to be
     * removed at a later point in the future.
     */
    fun unimplemented() {
        this.state = State.CLOSED
        TODO("Hit unimplemented instruction")
    }
}
