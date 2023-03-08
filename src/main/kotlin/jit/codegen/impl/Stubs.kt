package io.github.vbe0201.jiffy.jit.codegen.impl

import io.github.vbe0201.jiffy.jit.codegen.InstructionMeta
import io.github.vbe0201.jiffy.jit.codegen.Status
import io.github.vbe0201.jiffy.jit.codegen.jvm.BytecodeEmitter

/**
 * Emits a handler stub for unimplemented instructions.
 *
 * This should only be used during prototyping.
 */
fun unimplemented(meta: InstructionMeta, emitter: BytecodeEmitter): Status {
    println("[0x${meta.currentPc.toString(16)}] ${meta.insn.kind()} unimplemented!")

    emitter.unimplemented()
    return Status.TERMINATE_BLOCK
}
