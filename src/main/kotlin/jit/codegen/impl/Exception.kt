package io.github.vbe0201.jiffy.jit.codegen.impl

import io.github.vbe0201.jiffy.jit.codegen.InstructionMeta
import io.github.vbe0201.jiffy.jit.codegen.Status
import io.github.vbe0201.jiffy.jit.codegen.jvm.BytecodeEmitter

/**
 * Generates the System Call (SYSCALL) instruction to the code buffer.
 */
fun syscall(meta: InstructionMeta, emitter: BytecodeEmitter): Status {
    emitter.exception(meta.exceptionPc(), meta.branchDelaySlot, "SYSCALL")
    return Status.TERMINATE_BLOCK
}
