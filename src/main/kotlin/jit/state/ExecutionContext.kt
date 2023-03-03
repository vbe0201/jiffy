package io.github.vbe0201.jiffy.jit.state

import io.github.vbe0201.jiffy.cpu.BIOS_START
import io.github.vbe0201.jiffy.cpu.Bus
import io.github.vbe0201.jiffy.cpu.Cop0
import io.github.vbe0201.jiffy.cpu.STATUS_REGISTER

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
    val bus: Bus,
    /**
     * The [Cop0] unit used by the MIPS processor.
     */
    val cop0: Cop0
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
     * Gets a [Cop0] register at a given index.
     */
    @JvmName("getCop0Register")
    fun getCop0Register(index: UInt): UInt {
        return this.cop0.getRegister(index)
    }

    /**
     * Sets a [Cop0] register to a given value.
     */
    @JvmName("setCop0Register")
    fun setCop0Register(index: UInt, value: UInt) {
        this.cop0.setRegister(index, value)
    }

    /**
     * Reads an 8-bit value from the given memory address through
     * the CPU bus.
     */
    @JvmName("read8")
    fun read8(addr: UInt): UByte {
        if (this.cop0.status.cacheIsolated()) {
            return 0U
        }
        return this.bus.read8(addr)
    }

    /**
     * Reads a 16-bit value from the given memory address through
     * the CPU bus.
     */
    @JvmName("read16")
    fun read16(addr: UInt): UShort {
        if (this.cop0.status.cacheIsolated()) {
            return 0U
        }
        // TODO: Alignment check.
        return this.bus.read16(addr)
    }

    /**
     * Reads a 32-bit value from the given memory address through
     * the CPU bus.
     */
    @JvmName("read32")
    fun read32(addr: UInt): UInt {
        if (this.cop0.status.cacheIsolated()) {
            return 0U
        }
        // TODO: Alignment check.
        return this.bus.read32(addr)
    }

    /**
     * Writes an 8-bit value to a given memory address through the
     * CPU bus.
     */
    @JvmName("write8")
    fun write8(addr: UInt, value: UByte) {
        if (this.cop0.status.cacheIsolated()) {
            return
        }
        this.bus.write8(addr, value)
    }

    /**
     * Writes a 16-bit value to a given memory address through the
     * CPU bus.
     */
    @JvmName("write16")
    fun write16(addr: UInt, value: UShort) {
        if (this.cop0.status.cacheIsolated()) {
            return
        }
        // TODO: Alignment check.
        this.bus.write16(addr, value)
    }

    /**
     * Writes a 32-bit value to a given memory address through the
     * CPU bus.
     */
    @JvmName("write32")
    fun write32(addr: UInt, value: UInt) {
        if (this.cop0.status.cacheIsolated()) {
            return
        }
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
