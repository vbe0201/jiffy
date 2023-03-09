package io.github.vbe0201.jiffy.jit.codegen.impl

import io.github.vbe0201.jiffy.jit.codegen.InstructionMeta
import io.github.vbe0201.jiffy.jit.codegen.Status
import io.github.vbe0201.jiffy.jit.codegen.jvm.BytecodeEmitter

internal fun cop0(meta: InstructionMeta, emitter: BytecodeEmitter): Status {
    return when (val op = meta.insn.copOpcode()) {
        0b00000U -> mfc0(meta, emitter)
        0b00100U -> mtc0(meta, emitter)
        0b10000U -> rfe(meta, emitter)

        // TODO: Figure out a better way to handle invalid instructions.
        else -> {
            println("Unimplemented coprocessor instruction: $op")
            return unimplemented(meta, emitter)
        }
    }
}

/**
 * Generates the Move From Coprocessor 0 (MFC0) instruction to
 * the code buffer.
 */
fun mfc0(meta: InstructionMeta, emitter: BytecodeEmitter): Status {
    emitter.run {
        val res = emitter.getCop0Register(meta.insn.rd())
        configureDelayedLoad(meta.insn.rt(), res)
    }

    return Status.FILL_LOAD_DELAY_SLOT
}

/**
 * Generates the Move To Coprocessor 0 (MTC0) instruction to
 * the code buffer.
 */
fun mtc0(meta: InstructionMeta, emitter: BytecodeEmitter): Status {
    emitter.setCop0Register(meta.insn.rd()) {
        getGpr(meta.insn.rt())
    }

    return Status.CONTINUE_BLOCK
}

/**
 * Generates the Return From Exception (RFE) instruction to
 * the code buffer.
 */
fun rfe(
    @Suppress("UNUSED_PARAMETER") meta: InstructionMeta,
    emitter: BytecodeEmitter
): Status {
    // TODO: Check for unsupported virtual memory instructions?

    emitter.leaveException()
    return Status.CONTINUE_BLOCK
}
