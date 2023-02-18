package io.github.vbe0201.jiffy.jit.codegen

import io.github.vbe0201.jiffy.jit.codegen.impl.*
import io.github.vbe0201.jiffy.jit.decoder.Instruction
import io.github.vbe0201.jiffy.jit.state.ExecutionContext
import kotlin.system.exitProcess

private val handlerTable = arrayOf(
    ::unimplemented,
    ::unimplemented,
    ::unimplemented,
    ::unimplemented,
    ::unimplemented,
    ::unimplemented,
    ::unimplemented,
    ::unimplemented,

    ::unimplemented,
    ::unimplemented,
    ::unimplemented,
    ::unimplemented,
    ::unimplemented,
    ::ori,
    ::unimplemented,
    ::lui,

    ::unimplemented,
    ::unimplemented,
    ::unimplemented,
    ::unimplemented,
    ::unimplemented,
    ::unimplemented,
    ::unimplemented,
    ::unimplemented,

    ::unimplemented,
    ::unimplemented,
    ::unimplemented,
    ::unimplemented,
    ::unimplemented,
    ::unimplemented,
    ::unimplemented,
    ::unimplemented,

    ::unimplemented,
    ::unimplemented,
    ::unimplemented,
    ::unimplemented,
    ::unimplemented,
    ::unimplemented,
    ::unimplemented,
    ::unimplemented,

    ::unimplemented,
    ::unimplemented,
    ::unimplemented,
    ::unimplemented,
    ::unimplemented,
    ::unimplemented,
    ::unimplemented,
    ::unimplemented,

    ::unimplemented,
    ::unimplemented,
    ::unimplemented,
    ::unimplemented,
    ::unimplemented,
    ::unimplemented,
    ::unimplemented,
    ::unimplemented,

    ::unimplemented,
    ::unimplemented,
    ::unimplemented,
    ::unimplemented,
    ::unimplemented,
    ::unimplemented,
    ::unimplemented,
    ::unimplemented,
).also { check(it.size == 64) }

/**
 * Wraps a [BytecodeEmitter] and emits blocks of code to it.
 *
 * A block starts at any requested address and ends with a branch
 * instruction to the next target.
 *
 * After code has been emitted, [BytecodeEmitter.finish] may be
 * called on the underlying object.
 */
class BlockBuilder(
    /**
     * The [BytecodeEmitter] to populate.
     */
    val emitter: BytecodeEmitter
) {
    /**
     * Indicates whether an [Instruction] should be emitted.
     *
     * This is a shortcut to filter out instructions that do not require
     * codegen to achieve semantic equivalence in emulation.
     */
    fun shouldEmit(insn: Instruction): Boolean {
        // All-zero instructions are NOPs. We do not care about those.
        return insn.raw != 0U
    }

    /**
     * Builds a block of code out of the [ExecutionContext], starting
     * at a given address.
     *
     * Returns the size of all MIPS instructions governed by the resulting
     * block in bytes.
     *
     * The resulting code will be emitted to the inner [BytecodeEmitter].
     */
    fun build(context: ExecutionContext, addr: UInt): UInt {
        var offset = 0U

        var blockOpen = true
        while (blockOpen) {
            val insn = context.bus.readInstruction(addr + offset)

            // Emit the instruction implementation.
            if (shouldEmit(insn)) {
                // TODO: Figure out a nicer way to handle invalid instructions.
                val kind = insn.kind() ?: exitProcess(1)

                // Find the handler for the instruction and invoke it.
                val handler = handlerTable[kind.opcode.toInt()]
                blockOpen = handler(addr + offset, insn, this.emitter)
            }

            // Advance the offset from the original address by one instruction.
            offset += UInt.SIZE_BYTES.toUInt()
        }

        return offset
    }
}
