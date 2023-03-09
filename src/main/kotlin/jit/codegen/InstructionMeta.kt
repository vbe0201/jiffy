package io.github.vbe0201.jiffy.jit.codegen

import io.github.vbe0201.jiffy.jit.decoder.INSTRUCTION_SIZE
import io.github.vbe0201.jiffy.jit.decoder.Instruction

/**
 * Metadata for individual instructions during translation.
 *
 * Instances of this object are passed to each instruction handler
 * to provide them with context about their execution environment
 * without having to query it at runtime.
 *
 * Aids in emulating architectural quirks and having any relevant
 * data at hand for debugging.
 */
data class InstructionMeta(
    /** The raw [Instruction] data to be used during translation. */
    val insn: Instruction,

    /**
     * The MIPS Program Counter address at which the translated
     * instruction is located.
     */
    val currentPc: UInt,

    /**
     * The Program Counter value at the time of executing the
     * currently translated instruction.
     */
    val pc: UInt,

    /** This instruction specifically fills the branch delay slot. */
    val branchDelaySlot: Boolean,
) {
    /**
     * Gets the correct EPC value for the current instruction when an
     * exception must be generated.
     */
    inline fun exceptionPc(): UInt {
        var epc = this.currentPc

        // When an exception occurs, typically the PC at which the
        // instruction was placed is chosen as the EPC value; when
        // the exception occurs in a branch delay slot however, the
        // address of the prior branch must be used instead.
        if (this.branchDelaySlot) {
            epc -= INSTRUCTION_SIZE
        }

        return epc
    }
}
