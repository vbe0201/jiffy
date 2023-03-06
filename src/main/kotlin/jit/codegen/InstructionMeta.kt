package io.github.vbe0201.jiffy.jit.codegen

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
    /**
     * The raw [Instruction] data to be used during translation.
     */
    val insn: Instruction,

    /**
     * The MIPS Program Counter value of the executed instruction.
     */
    val pc: UInt,

    /**
     * This instruction specifically fills the branch delay slot.
     */
    val branchDelaySlot: Boolean,
)
