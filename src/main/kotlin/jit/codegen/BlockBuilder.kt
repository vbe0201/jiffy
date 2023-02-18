package io.github.vbe0201.jiffy.jit.codegen

import io.github.vbe0201.jiffy.jit.codegen.impl.*
import io.github.vbe0201.jiffy.jit.decoder.INSTRUCTION_SIZE
import io.github.vbe0201.jiffy.jit.decoder.Instruction
import io.github.vbe0201.jiffy.jit.state.ExecutionContext
import kotlin.system.exitProcess

private fun function(
    pc: UInt,
    insn: Instruction,
    emitter: BytecodeEmitter
): Status {
    // TODO: Figure out a more elegant way to handle invalid instructions.
    val func = insn.function() ?: exitProcess(1)

    // Delegate to the corresponding entry in the functions table.
    val handler = functionTable[func.opcode.toInt()]
    return handler(pc, insn, emitter)
}

private val functionTable = arrayOf(
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
    ::or,
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

private val handlerTable = arrayOf(
    ::function,
    ::unimplemented,
    ::j,
    ::unimplemented,
    ::unimplemented,
    ::bne,
    ::unimplemented,
    ::unimplemented,

    ::unimplemented,
    ::addiu,
    ::unimplemented,
    ::unimplemented,
    ::unimplemented,
    ::ori,
    ::unimplemented,
    ::lui,

    ::cop0,
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
    ::sw,
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
    private fun shouldEmit(insn: Instruction): Boolean {
        // All-zero instructions are NOPs. We do not care about those.
        return insn.raw != 0U
    }

    /**
     * Fetches the next [Instruction] from the given [ExecutionContext]
     * and adds it to the block of generated code.
     *
     * Returns the translation [Status] of the instruction.
     */
    private fun addInstruction(context: ExecutionContext): Status {
        val (addr, insn) = context.fetchNextInstruction()

        if (shouldEmit(insn)) {
            // TODO: Figure out a nicer way to handle invalid instructions.
            val kind = insn.kind() ?: exitProcess(1)

            // Find the handler for the instruction and invoke it.
            val handler = handlerTable[kind.opcode.toInt()]
            return handler(addr, insn, this.emitter)
        }

        return Status.CONTINUE_BLOCK
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
    fun build(context: ExecutionContext): UInt {
        var processed = 0U

        // Emit more instructions until the block is complete.
        var status = Status.CONTINUE_BLOCK
        while (status == Status.CONTINUE_BLOCK) {
            status = addInstruction(context)
            processed += INSTRUCTION_SIZE
        }

        // Check if we need to emit an additional instruction
        // for the pipeline delay slot after a branch.
        //
        // NOTE: Multiple consecutive branches are forbidden
        // by the MIPS manual, so we don't need to handle this.
        if (status == Status.FILL_DELAY_SLOT) {
            addInstruction(context)
            processed += INSTRUCTION_SIZE
        }

        return processed
    }
}
