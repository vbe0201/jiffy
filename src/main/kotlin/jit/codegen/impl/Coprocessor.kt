package io.github.vbe0201.jiffy.jit.codegen.impl

import io.github.vbe0201.jiffy.cpu.ExceptionKind
import io.github.vbe0201.jiffy.jit.codegen.InstructionMeta
import io.github.vbe0201.jiffy.jit.codegen.Status
import io.github.vbe0201.jiffy.jit.codegen.jvm.BytecodeEmitter

// TODO: COP2 supports these.

/** Emits an unsupported Coprocessor instruction to the code buffer. */
fun cop(meta: InstructionMeta, emitter: BytecodeEmitter): Status {
    emitter.exception(
        meta.exceptionPc(),
        meta.branchDelaySlot,
        ExceptionKind.COPROCESSOR_ERROR
    )
    return Status.TERMINATE_BLOCK
}

/** Emits an unsupported Load Word Coprocessor (LWC) to the code buffer. */
fun lwc(meta: InstructionMeta, emitter: BytecodeEmitter): Status {
    emitter.exception(
        meta.exceptionPc(),
        meta.branchDelaySlot,
        ExceptionKind.COPROCESSOR_ERROR
    )
    return Status.TERMINATE_BLOCK
}

/** Emits an unsupported Store Word Coprocessor (SWC) to the code buffer. */
fun swc(meta: InstructionMeta, emitter: BytecodeEmitter): Status {
    emitter.exception(
        meta.exceptionPc(),
        meta.branchDelaySlot,
        ExceptionKind.COPROCESSOR_ERROR
    )
    return Status.TERMINATE_BLOCK
}
