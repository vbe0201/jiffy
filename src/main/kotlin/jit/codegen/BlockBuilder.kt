package io.github.vbe0201.jiffy.jit.codegen

import io.github.vbe0201.jiffy.jit.codegen.jvm.BytecodeEmitter
import io.github.vbe0201.jiffy.jit.decoder.INSTRUCTION_SIZE
import io.github.vbe0201.jiffy.jit.state.ExecutionContext

/**
 * Translates a block of machine code to Java bytecode using the
 * provided [BytecodeEmitter].
 *
 * A block starts at any requested address and ends with a branch
 * instruction to the next block.
 *
 * After code has been emitted, [BytecodeEmitter.finish] may be
 * called on the underlying object.
 */
class BlockBuilder(
    /**
     * The underlying [BytecodeEmitter] to emit code to.
     */
    val emitter: BytecodeEmitter,
) {
    private val optimizer = Optimizer(this.emitter)

    private fun emitInstruction(
        context: ExecutionContext,
        addr: UInt,
        previousStatus: Status
    ): Status {
        // Read the instruction and construct its metadata.
        val meta = InstructionMeta(
            context.bus.readInstruction(addr),
            addr,
            previousStatus === Status.FILL_BRANCH_DELAY_SLOT
        )

        if (!this.optimizer.runInstructionFilters(meta)) {
            return dispatch(meta, this.emitter)
        }

        return Status.CONTINUE_BLOCK
    }

    /**
     * Builds a block of code out of the [ExecutionContext], starting
     * at a given MIPS address.
     *
     * Returns the size of all MIPS instructions in the block in bytes.
     *
     * The generated Java Bytecode can be obtained through calling
     * [BytecodeEmitter.finish].
     */
    fun build(context: ExecutionContext, addr: UInt): UInt {
        var count = 0U

        // Emit more instructions until the block is complete.
        var status = Status.CONTINUE_BLOCK
        while (status.blockOpen()) {
            val nextStatus = emitInstruction(context, addr + count, status)

            // When we have a branch delay slot to handle from the
            // previous instruction, we need to finish it after the
            // next instruction has executed.
            //
            // Note that load delay slots are additionally taken care
            // of at register writes and memory reads. This handles
            // the case where none of these operations are performed.
            if (status == Status.FILL_LOAD_DELAY_SLOT) {
                this.emitter.finishDelayedLoad()
            }

            status = nextStatus
            count += INSTRUCTION_SIZE
        }

        // Check if we need to emit an additional instruction for the
        // pipeline delay slot after a branch.
        //
        // NOTE: Multiple consecutive branches are bogus and forbidden
        // by the MIPS manual, so we don't need to handle that.
        if (status == Status.FILL_BRANCH_DELAY_SLOT) {
            emitInstruction(context, addr + count, status)
            count += INSTRUCTION_SIZE
        }

        return count
    }
}
