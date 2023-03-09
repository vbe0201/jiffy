package io.github.vbe0201.jiffy.jit.state

import io.github.vbe0201.jiffy.bus.Bus
import io.github.vbe0201.jiffy.cpu.*
import io.github.vbe0201.jiffy.memory.MemoryMap
import io.github.vbe0201.jiffy.memory.kseg1

/**
 * The execution context of the JIT.
 *
 * This class wraps raw execution state of the emulated MIPS processor,
 * which is accessed and manipulated by generated JIT code.
 */
class ExecutionContext(
    /** The [Bus] interface to use during program execution. */
    val bus: Bus,
    /** The [Cop0] unit used by the MIPS processor. */
    val cop0: Cop0
) {
    /** The current execution [State] of this context. */
    var state = State.INITIAL

    /** The general-purpose CPU registers. */
    @get:JvmName("getGprs")
    var gprs = UIntArray(32) { 0U }
        private set

    /**
     * The special-purpose program counter register where the next
     * instruction should be fetched.
     *
     * Hard-wired to start at the first BIOS instruction in KSEG1.
     */
    @set:JvmName("setPc")
    var pc = kseg1(MemoryMap.BIOS.start)

    /**
     * The special-purpose register which holds the high 32 bits of
     * multiplication result; remainder of division.
     */
    @get:JvmName("getHi")
    @set:JvmName("setHi")
    var hi = 0U

    /**
     * The special-purpose register which holds the low 32 bits of
     * multiplication result; quotient of division.
     */
    @get:JvmName("getLo")
    @set:JvmName("setLo")
    var lo = 0U

    /** Gets a [Cop0] register at a given index. */
    @JvmName("getCop0Register")
    fun getCop0Register(index: UInt): UInt {
        return this.cop0.getRegister(index)
    }

    /** Sets a [Cop0] register to a given value. */
    @JvmName("setCop0Register")
    fun setCop0Register(index: UInt, value: UInt) {
        this.cop0.setRegister(index, value)
    }

    /** Reads an 8-bit value from the given memory address. */
    @JvmName("read8")
    fun read8(addr: UInt): UByte {
        // TODO: Implement Data Cache.
        if (this.cop0.status.de()) {
            return 0U
        }

        return this.bus.read8(addr)
    }

    /** Reads a 16-bit value from the given memory address. */
    @JvmName("read16")
    fun read16(addr: UInt): UShort {
        // TODO: Implement Data Cache.
        if (this.cop0.status.de()) {
            return 0U
        }

        return this.bus.read16(addr)
    }

    /** Reads a 32-bit value from the given memory address. */
    @JvmName("read32")
    fun read32(addr: UInt): UInt {
        // TODO: Implement Data Cache.
        if (this.cop0.status.de()) {
            return 0U
        }

        return this.bus.read32(addr)
    }

    /** Writes an 8-bit value to a given memory address. */
    @JvmName("write8")
    fun write8(addr: UInt, value: UByte) {
        // TODO: Implement Data Cache.
        if (this.cop0.status.de()) {
            return
        }

        this.bus.write8(addr, value)
    }

    /** Writes a 16-bit value to a given memory address. */
    @JvmName("write16")
    fun write16(addr: UInt, value: UShort) {
        // TODO: Implement Data Cache.
        if (this.cop0.status.de()) {
            return
        }

        this.bus.write16(addr, value)
    }

    /** Writes a 32-bit value to a given memory address. */
    @JvmName("write32")
    fun write32(addr: UInt, value: UInt) {
        // TODO: Implement Data Cache.
        if (this.cop0.status.de()) {
            return
        }

        this.bus.write32(addr, value)
    }

    /**
     * Raises a CPU exception given the necessary details.
     *
     * [pc] is the faulting program counter, or the instruction
     * before it when in a branch delay slot.
     *
     * [delayed] indicates if the exception happened in a branch
     * delay slot.
     *
     * [kind] is the type of exception that occurred.
     */
    @JvmName("raiseException")
    fun raiseException(pc: UInt, delayed: Boolean, kind: ExceptionKind) {
        // Configure exceptional state in COP0.
        this.cop0.raiseException(pc, delayed, kind)

        // Determine the exception handler address and jump to it.
        this.pc = when (this.cop0.status.bev()) {
            true -> 0xBFC0_0180U
            false -> 0x8000_0080U
        }
    }

    /** Restores internal state when leaving exception mode. */
    fun leaveException() {
        this.cop0.leaveException()
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
