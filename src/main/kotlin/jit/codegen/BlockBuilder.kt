package io.github.vbe0201.jiffy.jit.codegen

import io.github.vbe0201.jiffy.jit.decoder.Instruction
import io.github.vbe0201.jiffy.jit.state.ExecutionContext

/**
 * Wraps a [BytecodeEmitter] and emits blocks of code to it.
 *
 * A block starts at any requested address and ends with a branch
 * instruction to the next target.
 *
 * After code has been emitted, [BytecodeEmitter.finish] may be
 * called on the underlying object.
 */
class BlockBuilder(val emitter: BytecodeEmitter) {
    /**
     * Indicates whether an [Instruction] should be emitted.
     *
     * This is a shortcut to filter out instructions that do not require
     * codegen to achieve semantic equivalence in emulation.
     */
    fun shouldEmit(insn: Instruction): Boolean {
        // All-zero instructions are NOPs.
        // We do not care about those.
        return insn.raw == 0U
    }

    /**
     * Builds a block of code out of the [ExecutionContext], starting
     * at a given address.
     *
     * Returns the MIPS program counter at the end of the selected block.
     *
     * The resulting code will be emitted to the inner [BytecodeEmitter].
     */
    fun build(context: ExecutionContext, addr: UInt): UInt {
        // TODO: Do this properly.
        val insn = context.bus.readInstruction(addr).kind()
        println("Translating instruction $insn")

        this.emitter.generateUnimplementedStub()

        return addr + 4U
    }
}
