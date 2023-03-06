package io.github.vbe0201.jiffy.jit.codegen

import io.github.vbe0201.jiffy.jit.codegen.jvm.BytecodeEmitter

/**
 * A code optimizer which aids in generating optimal Java Bytecode.
 *
 * That may sound contradictory at first, given how good the JIT
 * compilers of common JVM implementations are at optimizing
 * garbage code like the one from javac; our naively crafted
 * bytecode is of comparable quality, actually.
 *
 * However, a few common code patterns behave in a way that cannot
 * be taught to the JIT in a favorable manner. We specifically
 * focus on those and let the JIT do its job for the most part.
 */
@JvmInline
value class Optimizer(private val emitter: BytecodeEmitter) {
    /**
     * Recognizes and filters out NOP instructions.
     *
     * MIPS makes heavy use of NOPs to fill delay slots or bridge
     * intervals for hardware reconfiguration to take effect.
     *
     * There is no dedicated NOP opcode in the MIPS architecture;
     * common choices are arithmetic instructions which write to
     * the `$zero` register, most notably `sll $zero, $zero, 0`
     * with the unique bit pattern of being all zeroes.
     *
     * This pass is designed to recognize common NOP patterns and
     * instructing codegen to skip the instructions.
     */
    object NopFilter {
        fun filter(meta: InstructionMeta): Boolean {
            // TODO: More bit patterns to cover?
            return meta.insn.raw == 0U
        }
    }

    /**
     * Runs all available instruction filters on a given instruction
     * to determine if it needs to be emitted or not.
     */
    fun runInstructionFilters(meta: InstructionMeta): Boolean {
        return NopFilter.filter(meta)
    }
}
