package io.github.vbe0201.jiffy.jit.codegen.impl

import io.github.vbe0201.jiffy.cpu.ExceptionKind
import io.github.vbe0201.jiffy.jit.codegen.InstructionMeta
import io.github.vbe0201.jiffy.jit.codegen.Status
import io.github.vbe0201.jiffy.jit.codegen.jvm.BytecodeEmitter
import io.github.vbe0201.jiffy.jit.jitLogger

/** Generates a CPU exception for invalid instructions. */
fun invalid(meta: InstructionMeta, emitter: BytecodeEmitter): Status {
    emitter.exception(
        meta.exceptionPc(),
        meta.branchDelaySlot,
        ExceptionKind.ILLEGAL_INSTRUCTION
    )
    return Status.TERMINATE_BLOCK
}

/** Emits a handler stub for unimplemented instructions. */
fun unimplemented(meta: InstructionMeta, emitter: BytecodeEmitter): Status {
    jitLogger.error {
        val pc = meta.currentPc.toString(16)
        "${meta.insn.kind()} at 0x$pc unimplemented!"
    }

    emitter.unimplemented()
    return Status.TERMINATE_BLOCK
}
